import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

public class features {

	private static void indexReaderFunction(String indexFilePath) throws IOException{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexFilePath)));
		
		//Print the total number of documents in the corpus
		System.out.println("Total number of documents in the corpus: "+reader.maxDoc());		
		
		Terms vocabulary = MultiFields.getTerms(reader, "reviewtips");
		
		Double TfIdfScore;
		IndexSearcher searcher = new IndexSearcher(reader);
		
		// connecting to mongoDB on local port
		MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		DB db = mongoClient.getDB( "yelp" );
		DBCollection collection = db.getCollection("features_collection");
    	DBObject insertString;
    	
    	// iterating over index to get feature set for each unique category
    	DefaultSimilarity dSimi=new DefaultSimilarity();
    	//Get the segments of the index
		List<LeafReaderContext> leafContexts = reader.getContext().reader().leaves();
		for (int i = 0; i < leafContexts.size(); i++) {
			
			HashMap<String,Double> termTfIdfScore = new HashMap<>();
			LeafReaderContext leafContext=leafContexts.get(i);
			int startDocNo=leafContext.docBase;
			int numberOfDoc=leafContext.reader().maxDoc();
			final Terms terms = reader.getTermVector(i, "reviewsandtips");
			
			if (terms != null && terms.size() > 0) {
				TermsEnum termsEnum = terms.iterator();
				BytesRef term = null;
				while ((term = termsEnum.next()) != null){
					PostingsEnum de = MultiFields.getTermDocsEnum(leafContext.reader(),
							"reviewsandtips", 
							term);
					int doc;
					if(de !=null)
					{
						while((doc=de.nextDoc())!=PostingsEnum.NO_MORE_DOCS)
						{
					
							int docNumber=de.docID()+startDocNo;
							float normLength=dSimi.decodeNormValue(leafContext.reader().getNormValues("reviewsandtips").get(doc));
							float docLeng = 1/(normLength*normLength);
							
							float TF=(de.freq()/docLeng);
							//Get Document Frequency

							int df=reader.docFreq(new Term("reviewsandtips",term.utf8ToString()));
							float IDF=(float)Math.log10(1+(reader.maxDoc()/df));
							double relevanceScore=TF*IDF;
							/* filter1: searches for numbers, website names,words ending with 's,searches for words with dot
							 * one or two letter words specifically,words like aaaaaaa lolololo mapmapmapmap which has repeating sequences in it*/
							if ((term.utf8ToString().matches(".*\\d.*")) || 							
					        		  (term.utf8ToString().matches(".*.*\\b(www.|.com)\\b.*.*") ||
					        		  (term.utf8ToString().matches(".*'s.*")) ||
					        		  (term.utf8ToString().matches(".*\\..*")) ||
					        		  (term.utf8ToString().matches("^[a-zA-Z]{1,2}$")) ||
					        		  filterRepeatingChars(term.utf8ToString()))){
					        	  
					          }
					          else{
					        	  termTfIdfScore.put(term.utf8ToString(), relevanceScore);
					          }	

						}
					}
					
				}
				
				
			}
			Map<String, Double> sortedTfIdfScore = sortByComparator(termTfIdfScore);
			Integer count = 0;
			String featureSet = "";
			for (Map.Entry<String, Double> entity: sortedTfIdfScore.entrySet()){
				if (count != 10){
					featureSet = featureSet + " " + entity.getKey();
					count = count + 1;
				}
				else{
					break;
				}					
			}
			Document indexDoc = searcher.doc(i);
			insertString=new BasicDBObject("category",indexDoc.get("category")).append("features",featureSet);
			collection.insert(insertString);
			System.out.println("Feature Set generated for :" + indexDoc.get("category"));
			System.out.println("Features: " + featureSet);
		}		
		reader.close();

			}
			
	/*Function sorts a map on keys and returns the sorted map*/	
	
	private static Map<String, Double> sortByComparator(Map<String, Double> unsortMap) {
		 
		// Convert Map to List
		List<Map.Entry<String, Double>> list = 
			new LinkedList<Map.Entry<String, Double>>(unsortMap.entrySet());
 
		// Sort list with comparator, to compare the Map values
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
				return (o2.getValue()).compareTo(o1.getValue());
			}
		});
 
		// Convert sorted map back to a Map
		Map<String, Double> sortedMap = new LinkedHashMap<String, Double>();
		for (Iterator<Map.Entry<String, Double>> it = list.iterator(); it.hasNext();) {
			Map.Entry<String, Double> entry = it.next();
			sortedMap.put(entry.getKey(), entry.getValue());
		}
		return sortedMap;
	}
	
	/*Function checks if string has words like: aaaaaa, bobobobobo 
	 * which have 2 or more repeating characters or bunch of characters
	 */
	private static boolean filterRepeatingChars(String stringToMatch){
		Pattern p = Pattern.compile("(\\w\\w)\\1+");
		Matcher m = p.matcher(stringToMatch);
		if (m.find())
		{
		    return true;
		}
		else {
			return false;		
		}
	}	
public static void main(String[] args) throws IOException {
		
		String indexPath = "R:/IUB/Search/trainout";		
		indexReaderFunction(indexPath);	
		
	}

}
