import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;




public class createTrainingIndex {
	private final String sourceFilePath ="R:/IUB/Search/trainIndex/";
	private final String indexFilePath="R:/IUB/Search/trainout";
	private IndexWriter writer=null;
	private File indexDirectory=null;
	private createTrainingIndex()throws FileNotFoundException, CorruptIndexException, IOException{
		 
		
		try {
			long start=System.currentTimeMillis();
			createIndexWriter();
			checkFileValidity();
			closeIndexWriter();
			//statsOfDocument();
			long end=System.currentTimeMillis();
			System.out.println("Total time Taken :"+(end-start)+" milli seconds");
		}catch (Exception e){
			System.out.println("Exception occured");
		}
	}
	private void createIndexWriter()
	{
		try{
		Analyzer analyzer = new EnglishAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		//IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		indexWriterConfig.setOpenMode(OpenMode.CREATE);
		File indexDir=new File(indexFilePath);
		Directory fileSystemDirectory = FSDirectory.open(Paths.get(indexFilePath));
		writer = new IndexWriter(fileSystemDirectory, indexWriterConfig);
		//Directory dir = FSDirectory.open(Paths.get(indexFilePath));
		//writer = new IndexWriter(dir, iwc);		
		}catch(Exception e)
		{
			System.out.println("unable to open direcotry");
		}
	}
	private void checkFileValidity()
	{
		File filestoIndex=new File(sourceFilePath);
		List<String> fileNames = new ArrayList<String>();
		File[] listoffiles=filestoIndex.listFiles();
		for (File file : listoffiles) {
			try{
				if(!file.isDirectory()
						&& !file.isHidden()
						&& file.exists()
						&& file.canRead()
						//&& file.length()>0.0
						&& file.isFile() 	)
					{
						fileNames.add(file.getAbsolutePath());
						System.out.println(file.getName());
					
					}
			}catch(Exception e){
				System.out.println("sorry cannot index"+file.getAbsolutePath());}
		
		}	
			System.out.println();
			indexFiles(fileNames);

	
	}
	private void indexFiles(List<String> fileNames)
	{
	 	
		System.out.println("Creating Index in the given location");
	    for (String filesToBeParsed : fileNames) {
	       try{
	    	   String reviewsAndTips = "";
	    	   File filewithext = new File(filesToBeParsed); 
	    	   String fileext =filewithext.getName();
	    	   String category = FilenameUtils.removeExtension(fileext);
	    	   System.out.println(category);
	    	  // FileInputStream fstream = new FileInputStream(filesToBeParsed);
	    	   //BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
	    	  // String strline;
	    	  // while((strline = br.readLine())!= null)
	    	   //{
	    	  // reviewsAndTips += strline;//
	    	  // System.out.println(strline);
	    	   //}//
	    	   reviewsAndTips = new String(Files.readAllBytes(Paths.get(filesToBeParsed)));
	    	   reviewsAndTips = reviewsAndTips.replace("[", "").replace("]","");
	    	   Document luceneDoc = new Document();
	    	   luceneDoc.add(new StringField("category",category, Store.YES));
	    	   if (reviewsAndTips != "")
				{
	    		   luceneDoc.add(new TextField("reviewsandtips",reviewsAndTips,Field.Store.YES));
														
								}
	    	   writer.addDocument(luceneDoc);
	       }
	       catch(Exception e)
	        {
			e.printStackTrace();  
			System.out.println("Could not add: " + filesToBeParsed);
	        }
	
	    }
	}
 private void closeIndexWriter()
	{
		try{
			writer.forceMerge(1);
			writer.commit();
			writer.close();
		}catch(Exception e){
			System.out.println("Indexer cannot be deleted");
		}
	}
 public static void main(String[]args)
	{
		try{
			new createTrainingIndex();
					
		}catch (Exception ex){
			System.out.println("cannot start");
		}
	}



}
