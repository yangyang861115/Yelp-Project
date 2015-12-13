package yelp;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/*
 * generate query for each category
 * simply use each category as a new query
 * e.g. category=["Chinese Restaurants", "Hunan"], query = ["Chinese", "Restaurants","Hunan"]
 */
public class generateQueries {

    public Set<String> queries() throws UnknownHostException {
        Set<String> query = new TreeSet<String>();
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("yelp");
        DBCollection collections = db.getCollection("test_set");
        DBCursor cursor = collections.find();

        while (cursor.hasNext()) {
            DBObject bu = cursor.next();
            BasicDBList categories = (BasicDBList) bu.get("categories");
            for (Object r : categories) {
                //split the category by non-alphanumeric chars like commas, dots, etc
                String[] l = r.toString().split("[\\W]");
                for (String s : l) {
                    query.add(s.toLowerCase());
                }         
            }
        }
        return query;
    }

    public static void main(String[] args) throws IOException {
        generateQueries gq = new generateQueries();
        Set<String> querySet = gq.queries();
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream("/Users/yangyang/Desktop/lucene_index/queries2.txt"), "utf-8"));

        for (String query : querySet) {
            writer.write(query + "\n");
        }
        writer.close();
        // System.out.print(querySet);
    }
}
