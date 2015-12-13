package yelp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
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


/*
 * for each query,search through the index01, find the top 10 related business_id
 * generate two separate result files
 * one for review field, the other for tip field 
 */
public class getScores {
    public void searchFiles(String index, String queryString, int query_id, String field, Writer writer)
            throws IOException, ParseException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(index)));
        IndexSearcher searcher = new IndexSearcher(reader);
        Analyzer analyzer = new StandardAnalyzer();
        searcher.setSimilarity(new BM25Similarity());

        QueryParser parser = new QueryParser(field, analyzer);
        Query query = parser.parse(queryString);
        // System.out.println("Searching for: " + query.toString(field));

        TopDocs results = searcher.search(query, 1000);
        ScoreDoc[] hits = results.scoreDocs;

        int numTotalHits = results.totalHits;
        System.out.println(numTotalHits + " total matching documents for field: " + field);
        int rank = 1;
        for (int i = 0; i < hits.length; i++) {
            Document doc = searcher.doc(hits[i].doc);
            String s = String.format("%.4f", hits[i].score);
            // System.out.println(hits[i].score);
            // System.out.println("business_id: "+doc.get("business_id"));
            // System.out.println("name: " + doc.get("name") );
            // System.out.println("category: "+doc.get("category"));
            writer.write(query_id + "\tQ0\t" + doc.get("business_id") + "\t" + rank + "\t" + s + "\t" + "run-1\n");
            rank++;
        }

        reader.close();
    }

    public static void main(String[] args) throws IOException, ParseException {
        // TODO Auto-generated method stub
        String index = "/Users/yangyang/Desktop/lucene_index/index01";
        String filePath = "/Users/yangyang/Desktop/lucene_index/queries2.txt";
        String resultPath = "/Users/yangyang/Desktop/lucene_index/results/reviewFieldScores.txt";
        // String resultPath2 =
        // "/Users/yangyang/Desktop/lucene_index/results/tipFieldScores.txt";
        int query_id = 1;
        String query = "";
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        getScores score = new getScores();
        Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultPath), "utf-8"));
        // Writer writer = new BufferedWriter(new OutputStreamWriter(
        // new FileOutputStream(resultPath2), "utf-8"));
        while ((query = br.readLine()) != null) {

            score.searchFiles(index, query, query_id, "review", writer);
            // score.searchFiles(index, query, query_id, "tip", writer);
            query_id++;
        }
        writer.close();

    }

}
