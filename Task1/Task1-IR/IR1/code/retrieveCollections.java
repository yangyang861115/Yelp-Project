package yelp;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
/*
 * create a mongodb collection test_set for all business_id
 * each record in the collection includes 
 * business_id, name, categories, reviews, review stars, tips, tips likes 
 */
public class retrieveCollections {

    private MongoClient mongoClient;
    private DB db;
    private DBCollection collections;

    retrieveCollections(MongoClient mongoClient, DB db, DBCollection collections) {
        this.mongoClient = mongoClient;
        this.db = db;
        this.collections = collections;
    }

    private void createCollections(DB db, DBCollection collections) {
        DBCollection businessCollection = db.getCollection("business");
        DBCollection reviewCollection = db.getCollection("review");
        DBCollection tipsCollection = db.getCollection("tip");
        // values which needs to be retrieved are appended with 1 and values
        DBObject query = new BasicDBObject("_id", 0).append("business_id", 1).append("categories", 1).append("name", 1);
        DBCursor buCursor = businessCollection.find(new BasicDBObject(), query);
        int numberOfRecords = 0;
        while (buCursor.hasNext()) {
            DBObject bu = buCursor.next();
            DBObject document = new BasicDBObject(bu.toMap());
            String businessID = (String) bu.get("business_id");
            // System.out.println("business id from businees collection is: " +
            // businessID);
            // find the review text for each business id
            DBObject reviewQuery = new BasicDBObject("business_id", businessID);
            DBCursor reviewCursor = reviewCollection.find(reviewQuery);
            List reviewlist = new ArrayList();
            List reviewStars = new ArrayList();
            // System.out.println("entering into while reviewcursor loop");
            while (reviewCursor.hasNext()) {
                // System.out.println("entering into while loop");
                DBObject reviewObject = reviewCursor.next();
                String textdata = (String) reviewObject.get("text");
                reviewlist.add(textdata);
                // System.out.println("review: " + textdata);

                if (reviewObject.get("stars") == null)
                    reviewStars.add(0);
                else
                    reviewStars.add((int) reviewObject.get("stars"));
            }

            document.put("reviews", reviewlist);
            document.put("review stars", reviewStars);

            // find the tip text for each business id
            DBObject tipsQuery = new BasicDBObject("business_id", businessID);
            DBCursor tipsCursor = tipsCollection.find(tipsQuery);
            List tipslist = new ArrayList();
            List tipslikes = new ArrayList();

            while (tipsCursor.hasNext()) {
                DBObject tipsObject = tipsCursor.next();
                String tipdata = (String) tipsObject.get("text");
                tipslist.add(tipdata);
                // System.out.println("tip: " + tipdata);
                if (tipsObject.get("likes") == null)
                    tipslikes.add(0);
                else
                    tipslikes.add((int) tipsObject.get("likes"));
            }
            document.put("tips", tipslist);
            document.put("tips likes", tipslikes);

            collections.insert(document);
            numberOfRecords++;
        }
    }

    public static void main(String args[]) throws UnknownHostException {
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("yelp");
        DBCollection collections = db.getCollection("test_set");
        retrieveCollections collect = new retrieveCollections(mongoClient, db, collections);
        collect.createCollections(db, collections);
    }
}
