package index;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import index.SpecialAnalyzer;

public class Indexer {

    /**
     * Creates the initial index files on disk
     *
     * @param indexPath
     * @return
     * @throws IOException
     */
    private static IndexWriter setupIndex(String indexPath) throws IOException {
        Analyzer analyzer = new SpecialAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_46,
                analyzer);
        config.setOpenMode(OpenMode.CREATE);
        config.setRAMBufferSizeMB(2048.0);

        FSDirectory dir;
        IndexWriter writer = null;
        dir = FSDirectory.open(new File(indexPath));
        writer = new IndexWriter(dir, config);
        
        return writer;
    }

    /**
     * @param indexPath
     *            Where to create the index
     * @param prefix
     *            The prefix of all the paths in the fileList
     * @param fileList
     *            Each line is a path to a document
     * @throws IOException
     * @throws JSONException 
     */
    public static void index(String indexPath, String prefix)
            throws IOException, ParseException, JSONException {

        System.out.println("Creating Lucene index...");

        FieldType _contentFieldType = new FieldType();
        _contentFieldType.setIndexed(true);
        _contentFieldType.setStored(true);
        int indexed = 0;
        
        
    	IndexWriter writer = setupIndex(indexPath);
        
        final File folder= new File(prefix);
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
            	index(indexPath,fileEntry.toString());
            } else {
            	String extension = "";
            	int k = fileEntry.getName().lastIndexOf('.');
            	if (k>0){
            		extension = fileEntry.getName().substring(k+1);
            	}
            	if(extension.equals("json")){
   //         	IndexWriter writer = setupIndex(indexPath);
    //            BufferedReader br = new BufferedReader(
      //                  new FileReader(fileEntry));
        //        String line = null;
        /*        while ((line = br.readLine()) != null) {

                    Document doc = new Document();
                    doc.add(new Field("content", line, _contentFieldType));
                    writer.addDocument(doc);

                    ++indexed;
                    if (indexed % 10 == 0)
                        System.out.println(" -> indexed " + indexed + " docs...");
                }*/
                // Index Json Files
	                Object obj;
	                JSONObject jsonObject= new JSONObject();
	                JSONArray reviews = new JSONArray();
	                obj = new JSONTokener(new FileReader(prefix+fileEntry.getName())).nextValue();
	                if(obj.getClass().getName().equals("org.json.JSONObject")){
	                	jsonObject =  (JSONObject) obj;
	                	reviews= (JSONArray) jsonObject.getJSONArray("Reviews");
	                }
	                else{
	                	reviews= (JSONArray) obj;
	                }

	                System.out.println("Indexing: "+fileEntry.getName());
	                for(int i=0;i<reviews.length();i++){
	                	Document doc = new Document();          
	                	
	     //			For Temporary Index in One field
	     //          	String review = reviews.getJSONObject(i).toString();
	     //          	doc.add(new Field("content", review, _contentFieldType));
	                	
	       //		For Ramsey's crawled Data
	                	String abstractInfo="";
	                	String authors="";
	                	String title = "";
	                	String journalInfo="";
	                	String wos = "";
	                	String documentInfo="";
	                	String categories="";
	                	
	                	if(reviews.getJSONObject(i).has("Abstract"))
	                		abstractInfo = reviews.getJSONObject(i).getString("Abstract").toString();
	                	if(reviews.getJSONObject(i).has("Authors"))
	                		authors = reviews.getJSONObject(i).getString("Authors").toString();
	                	if(reviews.getJSONObject(i).has("Title"))
	                		title = reviews.getJSONObject(i).getString("Title").toString();
	                	if(reviews.getJSONObject(i).has("Journal Information"))
	                		journalInfo = reviews.getJSONObject(i).getString("Journal Information").toString();
	                	if(reviews.getJSONObject(i).has("Document Information")){	                		
	                		documentInfo = reviews.getJSONObject(i).getString("Document Information").toString();
	                		if(documentInfo.contains("WOS:")){
		                		wos = documentInfo.substring(documentInfo.indexOf("WOS:"));
		                		wos = wos.substring(4,wos.indexOf(" "));		                			
	                		}
	                	}
	                	if(reviews.getJSONObject(i).has("Categories"))
	                		categories = reviews.getJSONObject(i).getString("Categories").toString();
	                	
	                    doc.add(new Field("abstractInfo", abstractInfo, _contentFieldType));
	                    doc.add(new Field("authors", authors, _contentFieldType));
	                    doc.add(new Field("title", title, _contentFieldType));
	                    doc.add(new Field("journalInfo", journalInfo, _contentFieldType));
	                    doc.add(new Field("documentInfo", documentInfo, _contentFieldType));
	                    doc.add(new Field("categories", categories, _contentFieldType));
	                    doc.add(new Field("wos", wos, _contentFieldType));
	                    
	         // For old (Rizwan) cralwed Data      	
	 /*               	String Content = reviews.getJSONObject(i).getString("Content").toString();
	                	String Author = reviews.getJSONObject(i).getString("Author").toString();
	                	String Title = reviews.getJSONObject(i).getString("Title").toString();
	                	String Date = reviews.getJSONObject(i).getString("Date").toString();
	                	
	                	Document doc = new Document();
	                    doc.add(new Field("Content", Content, _contentFieldType));
	                    doc.add(new Field("Author", Author, _contentFieldType));
	                    doc.add(new Field("Title", Title, _contentFieldType));
	                    doc.add(new Field("Date", Date, _contentFieldType));*/

	                    writer.addDocument(doc);

   //                     System.out.println("indexed: " + title );
	                    ++indexed;
	                    if (indexed % 10 == 0)
	                        System.out.println(" -> indexed " + indexed + " docs...");
	                	
	                }
	           //     System.out.println(fileEntry.getName()+" indexing complete");
            	
            	
            	}
            }
        }
        
        
        
        
        /*IndexWriter writer = setupIndex(indexPath);
  //    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
        String line = null;
   //   int indexed = 0;
        while ((line = br.readLine()) != null) {

            Document doc = new Document();
            doc.add(new Field("content", line, _contentFieldType));
            writer.addDocument(doc);

            ++indexed;
            if (indexed % 10 == 0)
                System.out.println(" -> indexed " + indexed + " docs...");
        }
        // Index Json Files
        Object obj;
        JSONObject jsonObject= new JSONObject();
        JSONParser parser= new JSONParser();
        obj = new JSONTokener(new FileReader(prefix + fileList)).nextValue();
        jsonObject =  (JSONObject) obj;
        JSONArray reviews= jsonObject.getJSONArray("Reviews");
        for(int i=0;i<reviews.length();i++){
        	String review = reviews.getJSONObject(i).toString();
        	
        	Document doc = new Document();
            doc.add(new Field("content", review, _contentFieldType));
            writer.addDocument(doc);

            ++indexed;
            if (indexed % 10 == 0)
                System.out.println(" -> indexed " + indexed + " docs...");
        	
        }*/
        
        
        
        
        
        System.out.println(" -> indexed " + indexed + " total docs.");

   //     br.close();
        writer.close();
    }
}