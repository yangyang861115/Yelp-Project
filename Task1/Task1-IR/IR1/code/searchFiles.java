package yelp;

import java.io.IOException;
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
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/*
 * search query in index
 */
public class searchFiles {
    /*
     * find the top 10 related business_id and add them to the result
     */
    public void searchFiles(String index, String queryString, String field, Map<String, String> result)
            throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        searcher.setSimilarity(new BM25Similarity());

        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(queryString);
        // System.out.println("Searching for: " + query.toString(field));

        TopDocs results = searcher.search(query, 10);
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents for field: " + field);

        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            // System.out.println(hits[i].score);
            // System.out.println("business_id: "+doc.get("business_id"));
            // System.out.println("name: " + doc.get("name") );
            // System.out.println("category: "+doc.get("category"));
            result.put(doc.get("business_id"), doc.get("category"));
        }

        reader.close();
    }
    
    static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {
        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
            new Comparator<Map.Entry<K,V>>() {
                @Override 
                public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {
                    int res = e2.getValue().compareTo(e1.getValue());
                    return res != 0 ? res : 1;
                }
            }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    public static void main(String[] args) throws Exception {
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("yelp");
        DBCollection collections = db.getCollection("test_set");
        BasicDBObject query = new BasicDBObject();

        String index = "/Users/yangyang/Desktop/lucene_index/index01";
        String queryString = "go at night free drink";
        searchFiles sf = new searchFiles();

        Map<String, String> result = new HashMap<String, String>();
        sf.searchFiles(index, queryString, "review", result);
       
        sf.searchFiles(index, queryString, "tip", result);
        System.out.println("####################");
        // System.out.print(result);
        /*
         * result contains at most 20 related business_id find the categories
         * from test_set collection
         */
        Map<String, Integer> suggestion = new TreeMap<String, Integer>(); 
        for (String key : result.keySet()) {

            query.put("business_id", key);
            DBCursor cursor = collections.find(query);
            while (cursor.hasNext()) {
                DBObject tipsObject = cursor.next();
                BasicDBList categories = (BasicDBList) tipsObject.get("categories");
                
                System.out.println(categories);                
                for (Object category: categories) {
                    String stringkey = category.toString();
                    if(suggestion.containsKey(stringkey)) {
                        int val = suggestion.get(stringkey) + 1;
                        suggestion.put(stringkey, val);
                    } else {
                        suggestion.put(stringkey, 1);
                    }
                    
                }
            }
        }
        System.out.println("####################");
        System.out.println(suggestion);
        System.out.println("####################");
        for (Entry<String, Integer> entry  : entriesSortedByValues(suggestion)) {
            System.out.println(entry.getKey()+":"+entry.getValue());
        }
    }
}
