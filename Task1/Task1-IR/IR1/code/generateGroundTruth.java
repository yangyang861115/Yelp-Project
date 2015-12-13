package yelp;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import com.mongodb.BasicDBList;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
public class generateGroundTruth {

/*
 * generate ground truth file
 * for each query(category), check each business_id,
 * if the categories include the query, then the query and business_id is relevant
 * file format
 * Query       Q        BusinessID      Judgement
 */
    public static void main(String[] args) throws IOException {
        // TODO Auto-generated method stub
        //open the query file "queries2.txt"
        String filePath = "/Users/yangyang/Desktop/lucene_index/queries2.txt";
        String groundTruthFilePath = "/Users/yangyang/Desktop/lucene_index/groundtruth.txt";
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(groundTruthFilePath), "utf-8"));
        
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("yelp");
        DBCollection collections = db.getCollection("test_set");
       
        String query = "";
        int query_id = 1;
        while ((query = br.readLine()) != null) {
            System.out.println(query_id + " " + query);
            DBCursor cursor = collections.find();
            while (cursor.hasNext()) {
                DBObject bu = cursor.next();
                String business_id = (String) bu.get("business_id");
                BasicDBList categories = (BasicDBList) bu.get("categories");
                String category = "";
                for (Object r : categories) {
                    category += r.toString() + " ";
                }
                category = category.toLowerCase();
                //System.out.println(category);
                if (category.contains(query)) {
                    writer.write(query_id + "\t" + "Q0\t" + business_id + "\t1\n");
                }
            }
            System.out.println("############");
            query_id++;
        }
        writer.close();

    }

}
