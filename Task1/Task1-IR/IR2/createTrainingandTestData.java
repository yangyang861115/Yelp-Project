package lucene4;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class createTrainingandTestData {
	private MongoClient mongoClient;
	private DB db;
	private DBCollection traingingCollections;
	private DBCollection testCollections;
	createTrainingandTestData(MongoClient mongoClient,DB db,DBCollection trainingCollection,DBCollection testCollection){
		this.mongoClient=mongoClient;
		this.db=db;
		this.traingingCollections=trainingCollection;
		this.testCollections = testCollection;
		
	}
private void createCollections(DB db,DBCollection trainingCollection,DBCollection testCollection){
		
		DBCollection businessCollection=db.getCollection("business");
		DBCollection reviewCollection=db.getCollection("review");
		DBCollection tipsCollection=db.getCollection("tip");
		//values which needs to be retreived are appended with 1 and values 
		DBObject query = new BasicDBObject("_id",0).append("business_id", 1).append("categories", 1).append("name", 1);
		DBCursor buCursor=businessCollection.find(new BasicDBObject(),query);
		int numberOfRecords = 0;
		while(buCursor.hasNext()){
			DBObject bu = buCursor.next();
			DBObject document = new BasicDBObject(bu.toMap());
			String businessID=(String)bu.get("business_id");
			//System.out.println("business id from businees collection is"+businessID);
			DBObject reviewQuery=new BasicDBObject("business_id",businessID);
			DBCursor reviewCursor=reviewCollection.find(reviewQuery);
			List reviewlist = new ArrayList();
			//System.out.println("entering into while reviewcursor loop");
			while(reviewCursor.hasNext())
			{
				//System.out.println("entering into while loop");
				DBObject reviewObject=reviewCursor.next();
				reviewlist.add((String) reviewObject.get("text"));
				String textdata = (String)reviewObject.get("text");
				//System.out.println("business id from businees collection is"+textdata);
				
				}
			
			document.put("reviews",reviewlist);
			
			
			DBObject tipsQuery = new BasicDBObject("business_id",businessID);
			DBCursor tipsCursor = tipsCollection.find(tipsQuery);
			List tipslist = new ArrayList();
			
			
			while (tipsCursor.hasNext()){
				DBObject tipsObject = tipsCursor.next();
				tipslist.add((String) tipsObject.get("text"));
					
			}
			document.put("tips", tipslist);
			
			if(numberOfRecords <= 40000){
				
				trainingCollection.insert(document);
				numberOfRecords++;
			}
			else {
				testCollection.insert(document);
				numberOfRecords++;
			}
			
		}		
		}
		
	
	public static void main(String args[]) throws UnknownHostException{
		MongoClient mongoClient=new MongoClient();
		DB  db=mongoClient.getDB("yelp"); 
		DBCollection trainingCollection=db.getCollection("training_collection");
		DBCollection testCollection = db.getCollection("test_collection");
		createTrainingandTestData collect=new createTrainingandTestData(mongoClient,db,trainingCollection,testCollection);
		collect.createCollections(db,trainingCollection,testCollection);
	}
}



