import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.util.Version;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;


public class createTestIndex {
	private  MongoClient mongoClient;
	private  DB db;
	createTestIndex(MongoClient mongoClient,DB db) throws UnknownHostException
	{
		this.mongoClient = mongoClient;
		this.db = db;
	}
	/* indexed business id , categories, name, review and tips which will be extracted from test collection*/
	private  void  createIndex(String indexFilePath,Analyzer analyzer) throws IOException,
	FileNotFoundException {
		
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		Directory fileSystemDirectory = FSDirectory.open(Paths.get(indexFilePath));
		IndexWriter writer = new IndexWriter(fileSystemDirectory, indexWriterConfig);

		//MongoDB test collection name
		DBCollection collection = db.getCollection("test_collection");

		//Preparing Query to fetch all the documents from the test collectio
		DBObject projectionString=new BasicDBObject("_id",0);
		DBObject queryString=new BasicDBObject();
		DBCursor cursor = collection.find(queryString,projectionString).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
		DBObject result;

		//Declaring variables to store field values from the query result
		String businessID;
		List categories;
		String businessName;
		List reviews;
		
		List tips;
				

		try{	
			//Iterating over query results
			while (cursor.hasNext())
			{
				result=cursor.next();
				//Creating a lucene document
				Document luceneDoc = new Document();

				//Adding fields to the lucene document
				businessID=result.get("business_id").toString();
				luceneDoc.add(new StringField("business_id",businessID, Store.YES));

				businessName=result.get("name").toString();
				luceneDoc.add(new StringField("business_name",businessName, Store.YES));

				String reviewsAndTips="";
				categories=(List)result.get("categories");
				for (Object category:categories)
				{
					luceneDoc.add(new StringField("categories",category.toString(), Store.YES));
				}

				//Preparing a concatenated string of reviews and tips for each business ID
				reviews=(List)result.get("reviews");

				for (Object review:reviews)
				{
					reviewsAndTips+=review.toString();
				}

				tips=(List)result.get("tips");
				for (Object tip:tips)
				{
					reviewsAndTips+=tip.toString();
				}

				luceneDoc.add(new TextField("reviewsandtips",reviewsAndTips, Store.YES));
				// Write the lucene document to the index
				writer.addDocument(luceneDoc);					
			}		
		}
		finally
		{		
		cursor.close();
		writer.forceMerge(1);
		writer.commit();
		writer.close();								
		}
	}		

	public static void main(String[] args) throws CorruptIndexException,
	LockObtainFailedException, IOException{


		MongoClient mongoClient=new MongoClient( "localhost" , 27017 );
		DB db=mongoClient.getDB( "yelp" );
		String indexDir="R:/IUB/Search/testIndex/";
		createTestIndex indexGenerator=new createTestIndex(mongoClient,db);
		indexGenerator.createIndex(indexDir,new EnglishAnalyzer());

	}


}
