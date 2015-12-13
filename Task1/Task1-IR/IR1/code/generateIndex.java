package yelp;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * Index all records in  test_set collection under a directory by lucene.
 */
public class generateIndex {
    /**
     * Indexes a single document
     * 
     * @throws IOException
     */
    static void indexDoc(IndexWriter writer, HashMap<String, String> document) throws IOException {
        // make a new, empty document
        Document lDoc = new Document();
        lDoc.add(new StringField("business_id", document.get("business_id"), Field.Store.YES));
        lDoc.add(new TextField("name", document.get("name"), Field.Store.YES));
        lDoc.add(new TextField("category", document.get("category"), Field.Store.YES));
        lDoc.add(new TextField("review", document.get("review"), Field.Store.YES));
        lDoc.add(new TextField("tip", document.get("tip"), Field.Store.YES));
        writer.addDocument(lDoc);
    }

    public static void main(String[] args) throws IOException {
        // connect to the database and get the collection test_set
        MongoClient mongoClient = new MongoClient();
        DB db = mongoClient.getDB("yelp");
        DBCollection collections = db.getCollection("test_set");
        DBCursor cursor = collections.find();
       // ArrayList<HashMap<String, String>> documents = new ArrayList<HashMap<String, String>>();
        
        String indexPath = "/Users/yangyang/Desktop/lucene_index/index01";
        System.out.println("Indexing to directory '" + indexPath + "'...");
        Directory dir = FSDirectory.open(Paths.get(indexPath));
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

        iwc.setOpenMode(OpenMode.CREATE);

        IndexWriter writer = new IndexWriter(dir, iwc);
        int count = 0;
        while (cursor.hasNext()) {
            DBObject bu = cursor.next();
            String business_id = (String) bu.get("business_id");
            String name = (String) bu.get("name");
            BasicDBList categories = (BasicDBList) bu.get("categories");
            BasicDBList reviews = (BasicDBList) bu.get("reviews");
            BasicDBList tips = (BasicDBList) bu.get("tips");
            // change BasicDBList to String
            String category = "";
            for (Object r : categories) {
                category += r.toString() + " ";
            }
            String review = "";
            for (Object r : reviews) {
                review += r.toString() + " ";
            }
            String tip = "";
            for (Object r : tips) {
                tip += r.toString() + " ";
            }

            // create document for each business_id
            HashMap<String, String> document = new HashMap<String, String>();
            document.put("business_id", business_id);
            document.put("name", name);
            document.put("category", category);
            document.put("review", review);
            document.put("tip", tip);
            
            indexDoc(writer, document);
            count++;
        }
        writer.close();
        System.out.println("Finishing indexing " + count + " documents...");
    }

}
