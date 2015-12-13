package lucene4;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.store.LockObtainFailedException;

import com.mongodb.BasicDBObject;
import com.mongodb.Bytes;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class createTrainingCategories {
	private  MongoClient mongoClient;
	private  DB db;
	createTrainingCategories(MongoClient mongoClient,DB db) throws UnknownHostException
	 {
			this.mongoClient = mongoClient;
			this.db = db;
	 }
	public static void main(String[] args) throws CorruptIndexException,
	LockObtainFailedException, IOException{
	
	//Prepare connection to MongoDB with the YELP database
	MongoClient mongoClient=new MongoClient( "localhost" , 27017 );
	DB db=mongoClient.getDB( "yelp" );
	
	//Create index on Training data set
	//Path to create the lucene training index directory
	File indexDir=new File("R:/IUB/Search/trainIndex/");
	createTrainingCategories indexGenerator=new createTrainingCategories(mongoClient,db);
	indexGenerator.createTrainingIndex(indexDir,new EnglishAnalyzer());
	}
	private void  createTrainingIndex(File indexDir,Analyzer analyzer) throws IOException,
	FileNotFoundException {
		String outputFile; 
		File dir = new File("R:/IUB/Search/trainIndex/");
		DBCollection collection = db.getCollection("training_collection");
		DBObject projectionString=new BasicDBObject("_id",0).append("reviews",1).append("tips",1);
		DBObject fetchCategoriesQueryString;
		DBCursor cursor = null;
		DBObject result;
		PrintWriter writer = null;
		
		//Query to fetch all the distinct categories from the training data set and eliminating all null categories
		List<String> categoriesList = (List<String>)collection.distinct("categories");
		categoriesList.removeAll(Collections.singleton(null));		
		String  reviewsAndTips= null;
			
				for (String category:categoriesList)
				{
		
					outputFile = category +".txt";
					outputFile = outputFile.replace("/", "");
					File out = new File (dir, outputFile);
					System.out.println("Output File Name:" +outputFile);
					out.createNewFile();
					writer = new PrintWriter(out, "ASCII");
					fetchCategoriesQueryString=new BasicDBObject("categories",category);
					cursor=collection.find(fetchCategoriesQueryString,projectionString).addOption(Bytes.QUERYOPTION_NOTIMEOUT);
					while(cursor.hasNext())
					{
						result=cursor.next();
						writer.println(result.get("reviews"));
						writer.println(result.get("tips"));
					}
				}
				
		writer.close();
	}

}


