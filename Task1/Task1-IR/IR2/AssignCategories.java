
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
	public class AssignCategories {
		private MongoClient mongoClient;
		private DB db;
		private DBCollection featureCollection;
		private DBCollection outputCollection;
		private String indexDirPath;
		private Similarity rankingAlgorithm;
		
		AssignCategories(MongoClient mongoClient,DB db,DBCollection featureCollection,DBCollection outputCollection,String indexDirPath,Similarity rankingAlgorithm)
		{
			this.mongoClient=mongoClient;
			this.db=db;
			this.featureCollection=featureCollection;
			this.outputCollection=outputCollection;
			this.indexDirPath=indexDirPath;
			this.rankingAlgorithm=rankingAlgorithm;
		}
		
		public static void main(String[] args) throws ParseException, IOException
		{
			//Defining required parameters to create an object of the "AssignCategoriesToTestData" class
			
			// Index Directory Path for the Test Data
			String indexDirPath="R:/IUB/Search/testIndex/";
			
			//Prepare connection to MongoDB with the YELP database
			MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
			DB db = mongoClient.getDB( "yelp" );
			
			//"feature_set" collection stores the unique categories from the training set and their corresponding top N features
			DBCollection featureCollection = db.getCollection("feature_set");
			
			//"categories_assigned_from_code" stores the categories assigned programmatically for each business ID in the test collection
			DBCollection outputCollection = db.getCollection("categories_assigned_from_code");
			
			//Ranking algorithm to be used [BM25/Dirichlet]
			Similarity rankingAlgorithm=new BM25Similarity();
			
			AssignCategories computeCategories=new AssignCategories(mongoClient,db,featureCollection,outputCollection,indexDirPath,rankingAlgorithm);
			
			//Fetch similar categories from "CategorySimiliarityComparer" class to increase Recall
			
			HashMap<String, ArrayList<String>> groupedCategories=computeCategories.returnGroupedCollections();
			
			
			
			//Initializing QueryParser with EnglishAnalyzer on "reviewsandtips" field of lucene index on test data
			Analyzer analyzer = new EnglishAnalyzer();
			QueryParser parser = new QueryParser("reviewsandtips", analyzer);
			
			//Declaring variables necessary for MongoDB query operations
			String queryString;
			DBCursor outputCursor = null;
			DBObject outputResult=null;
			DBObject outputQueryString=null;
			DBObject queryString1=new BasicDBObject();
			DBCursor cursor=featureCollection.find(queryString1);
			DBObject result;
			String category;
			String query;
			
			
			
			//Initialize Index Reader and searcher
			IndexReader reader = DirectoryReader .open(FSDirectory.open(Paths.get(indexDirPath)));
			IndexSearcher searcher = new IndexSearcher(reader);
			searcher.setSimilarity(rankingAlgorithm); 
			
			//Iterating over each category and its top N features in the feature_set collection. Each category's feature is a query against the lucene test index.
			while (cursor.hasNext())
			{
				result=cursor.next();
				category=(String) result.get("category");
				query=(String) result.get("features");
				//Call "assignCategories" method to assign the computed categories to the test data set
				computeCategories.assignCategories(searcher, parser, query,outputCollection,outputCursor,outputResult,outputQueryString,category,10,groupedCategories);
			
				}
			//Close the Index Reader
			reader.close();
			
			
		}

/**
 * This method is used to remove duplicate categories assigned by code that matches with the existing categories in the data set for a business ID
 * @param categories
 * @return filtered categories list without duplicates
 */

		private  String removeDuplicates(String categories) {
			List<String> list = Arrays.asList(categories.split(","));
			List<String> uniqueList = new ArrayList<>();
			List<String> dupsRemoved = new ArrayList<>();
			String dupsRemovedStr = "";
			for (String eachItem : list){
				uniqueList.add(eachItem.trim());			
			}
			for (String eachItem : uniqueList){
				if (!dupsRemoved.contains(eachItem)){
					dupsRemoved.add(eachItem);
				}			
			}
			for (String eachitem : dupsRemoved){
				dupsRemovedStr += eachitem + ", ";
			}
	        return dupsRemovedStr.substring(0, dupsRemovedStr.length()-2);
		}
		
/***
 * This method is used to assign programmatically computed categories to the test data 

 */
		private  void assignCategories(IndexSearcher searcher,
				QueryParser parser, String queryString,DBCollection outputCollection,DBCursor outputCursor,DBObject outputResult,DBObject outputQueryString,String category,int numberOfRankedResults,HashMap<String, ArrayList<String>> groupedCategories) throws ParseException,
				IOException {
			
			DBObject insertString;
			DBObject updateString;
			DBObject searchString;
			DBObject updateCategory;
			String categories;
			
			//Check if the feature set is not empty or null
			if ((!queryString.equals("")) && (null!=queryString))
			{
					//Query against the lucene test data index to get the top N results for a given feature set as query
					Query query = parser.parse(queryString);
					TopDocs results=searcher.search(query,25);
					int numTotalHits=results.totalHits;
					ScoreDoc[] docs=results.scoreDocs;

					
					//Fetch similar categories for a given category to increase Recall
					if (groupedCategories.keySet().contains(category) && groupedCategories.get(category).size()>0 )
						{
						category+=","+ (groupedCategories.get(category).toString().replace("[","").replace("]", "").replace(", ",","));
						}
					//Assign the computed categories to the business IDs in the "categories_assigned_from_code" collection
					for (int i = 0; i < docs.length; i++) 
					{
						Document doc = searcher.doc(docs[i].doc);
						outputQueryString=new BasicDBObject("business_id",doc.get("business_id"));
						outputCursor=outputCollection.find(outputQueryString);
						
						//If the category field of the business is empty do an insert operation
						if (!outputCursor.hasNext())
						{
							insertString=new BasicDBObject("business_id",doc.get("business_id")).append("categories",category);
							outputCollection.insert(insertString);
						}
						//If the category field of the business is not empty do an update operation i.e. append the new categories to the existing categories
						else
						{
							outputResult=outputCursor.next();
							categories=(String) outputResult.get("categories")+","+category;
							
							//Check and remove the categories assigned by code that are already present in the database for the given business ID
							categories=removeDuplicates(categories);
		
							searchString=new BasicDBObject("business_id",doc.get("business_id"));
							updateCategory=new BasicDBObject("categories",categories);
							updateString=new BasicDBObject("$set",updateCategory);
							outputCollection.update(searchString, updateString);
						}
						
					}
			
		    }
		}
	

		/*grouped hashMap for similar categories*/
		
		public  HashMap<String, ArrayList<String>> returnGroupedCollections() throws UnknownHostException {
			HashMap<String,String> categoryFeatureSet = new HashMap<>();
			HashMap<String, String[]> mapWithSplitStrings = new HashMap<>();
			HashMap<String, ArrayList<String>> groupings = new HashMap<>();
			
			// Connecting to mongoDB to read stored feature set for each unique category
			MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
			DB db = mongoClient.getDB( "yelp" );
			DBCollection featureCollection = db.getCollection("feature_set");
			DBObject queryString=new BasicDBObject();
			DBCursor cursor=featureCollection.find(queryString);
			DBObject result;
			while (cursor.hasNext())
			{
				result=cursor.next();
				categoryFeatureSet.put(result.get("category").toString(),result.get("features").toString());
			}
			
			for (String key : categoryFeatureSet.keySet()) {
			    String value = categoryFeatureSet.get(key);
			    String[] words = value.split("\\s+");		    
			    mapWithSplitStrings.put(key, words);
			}

			Integer countCommon;
			for (String keyA : mapWithSplitStrings.keySet()) {
			    String[] valueSetA = mapWithSplitStrings.get(keyA);
			    ArrayList<String> commonSet = new ArrayList<>();
			   
			    for (String keyB : mapWithSplitStrings.keySet()){
				    if (keyB != keyA){
				    	String valueSetB[] = mapWithSplitStrings.get(keyB);
				    	countCommon = findCommon(valueSetA, valueSetB);			    		
				    	if (countCommon > 7){
				    		commonSet.add(keyB);
				    	}
				    }
				}
			    groupings.put(keyA,commonSet);
			}
			return groupings;
		}
		/*Integer: that is intersection count of two lists*/
		public  Integer findCommon(String[] listA, String[] listB){
			String arrayToHash[] = listA;
			String arrayToSearch[] = listB;
			
			HashSet<String> intersection = new HashSet<>();
			HashSet<String> hashedArray = new HashSet<>();
			
			for( String entry : arrayToHash){
				hashedArray.add(entry);
			}
			
			for( String entry : arrayToSearch){
				if(hashedArray.contains(entry)){
					intersection.add(entry);
				}
			}
			
			return intersection.size();
		}

	}
