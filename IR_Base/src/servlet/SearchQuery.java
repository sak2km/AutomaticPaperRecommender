package servlet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.json.JSONArray;
import org.json.JSONException;
//import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;

import org.json.JSONObject;
import org.json.JSONTokener;

import Analyzer.DocAnalyzer;
import Classifier.supervised.SVM;
import Ranker.SVMRanker;
import crawler.WebOfScienceCrawler;
import index.Indexer;
import index.ResultDoc;
import index.SearchResult;
import index.Searcher;
import index.similarities.OkapiBM25;
//import index.similarities.BooleanDotProduct;
//import index.Searcher;
//import index.ResultDoc;
//import index.SearchResult;
import mains.PaperReco;
import opennlp.tools.util.InvalidFormatException;
import structures.MyPriorityQueue;
import structures._Doc;
import structures._RankItem;

/**
 * Servlet implementation class SearchQuery
 */
@WebServlet("/SearchQuery")
public class SearchQuery extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public SearchQuery() {
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	//	response.getWriter().append("Served at: ").append(request.getContextPath());

		String searchKeyword = request.getParameter("search");
//		Map<String,String> env = System.getenv();
//	    String _prefix = "../../../../../Documents/Research/AutomaticPaperSelector/IR_Base/data/NewData/";
	
//	    try {		// for testing
//			Indexer.index(_indexPath, _data_path);
//		} catch (ParseException | JSONException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		
		System.out.println("Incoming query: "+searchKeyword);
//		System.getenv("MyLuceneIndexPath");
		response.setContentType("application/json");
		JSONObject jsonObject= new JSONObject();
		if(searchKeyword.equals("MachineLearning")){
			try {
				jsonObject =  generateRank();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
		}
		else{
			jsonObject = searchIndex(searchKeyword);	// Do search
		
		/****	Just return raw crawled data if keyword matches the json file name. To be deleted	****/	
/*			JSONParser parser= new JSONParser();
			 try{
				 String filePath = "/Users/AustinKim/Documents/Research/AutomaticPaperSelector/IR_Base/data/NewData/"+searchKeyword+".json";
				 File f = new File(filePath);
				 Object obj;
				 if(f.exists() && !f.isDirectory()) { 
					 obj = parser.parse(new FileReader(filePath));
					 System.out.println(searchKeyword+" exists!");
				 }
				 else{
					 System.out.println(searchKeyword+" not found! Crawling from WOS triggered");
					 return;
				 }
				 obj = new JSONTokener(new FileReader(filePath)).nextValue();//parser.parse(new FileReader(filePath));
		
				 jsonObject =  (JSONObject) obj;
	
			 }catch (FileNotFoundException e) { 
				 e.printStackTrace();
			 } catch (IOException e) {
				 e.printStackTrace();
			 } catch (ParseException e) {
				 e.printStackTrace();
			 } catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
	
		}
		 // Get the printwriter object from response to write the required json object to the output stream      
		 PrintWriter out = response.getWriter();
		 // Assuming your json object is **jsonObject**, perform the following, it will return your json object  
		 out.print(jsonObject);
		 out.flush();
		
		
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}
	public JSONObject searchIndex(String searchKeyword) throws MalformedURLException, IOException{
		JSONObject jsonObject= new JSONObject();
//		String _indexPath = "../../../../../Documents/Research/AutomaticPaperSelector/IR_Base/lucene-paper-index";
//		Searcher searcher = new Searcher(_indexPath);
		String _indexPath = System.getenv().get("Index_path");
		
		OkapiBM25 BM25 =  new OkapiBM25();
	    Searcher searcher = new Searcher(_indexPath);
		searcher.setSimilarity(BM25);	// Search by BM25 feature for now
		SearchResult result = searcher.search(searchKeyword, "abstractInfo");	//Do Search based on field "abstractInfo".
	    ArrayList<ResultDoc> results = result.getDocs();
	    
 //		if the list is empty, trigger crawler    
	    if(results.isEmpty()){
	    	try {
	    		System.out.println("No documents found. Crawler triggered.");
				crawlDoc(searchKeyword);
				return searchIndex(searchKeyword);
	//			return jsonObject;
			} catch (FailingHttpStatusCodeException | ParseException | JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
        JSONArray annoattedReviews = new JSONArray();
        
        int rank = 1;
        for (ResultDoc rdoc : results) {
        	try {
    	        JSONObject rankeditem= new JSONObject();
				int docId = rdoc.id();
				
		//		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(_indexPath)));
	    //      IndexSearcher indexSearcher = new IndexSearcher(reader);
			    Document document = searcher.getIndexSearcher().doc(docId);
			    
			    if(document.getField("authors")!=null)
			    	rankeditem.put("authors", document.getField("authors").stringValue().toString());
			    if(document.getField("title")!=null)
			    	rankeditem.put("title", document.getField("title").stringValue().toString());
			    if(document.getField("journalInfo")!=null)
			    	rankeditem.put("journalInfo", document.getField("journalInfo").stringValue().toString());
			    if(document.getField("documentInfo")!=null)
			    	rankeditem.put("documentInfo", document.getField("documentInfo").stringValue().toString());
			    if(document.getField("categories")!=null)
			    	rankeditem.put("categories", document.getField("categories").stringValue().toString());
			    rankeditem.put("docId", rdoc.title());
			    rankeditem.put("abstrct", rdoc.content());
			    if(document.getField("Snippet")!=null)
			    	rankeditem.put("Snippet", result.getSnippet(rdoc));
				annoattedReviews.put(rankeditem);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            System.out.println("\n------------------------------------------------------");
            System.out.println(rank + ". " + rdoc.title());
            System.out.println("------------------------------------------------------");
            System.out.println(result.getSnippet(rdoc)
                    .replaceAll("\n", " "));
            ++rank;
        }

        try {
			jsonObject.put("Reviews", annoattedReviews);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonObject;
	}
	
	
	public void crawlDoc(String searchKeyword) throws FailingHttpStatusCodeException, MalformedURLException, IOException, ParseException, JSONException{
    	WebOfScienceCrawler c = new WebOfScienceCrawler(searchKeyword, 1);	// #of pages to be crawled hard-coded
		try {
			ArrayList<json.JSONObject> crawledData = c.crawl(); //crawledData.get(0).get("Document Information")
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = new Date();
			String fileName = searchKeyword+dateFormat.format(date)+".json";
			
			System.out.println(crawledData);
			FileWriter file = new FileWriter("../../../../../Documents/Research/AutomaticPaperSelector/IR_Base/data/NewData/"+fileName);
			file.write(crawledData.toString());
			System.out.println("Copied Crawled JSON to file "+ fileName);
			file.flush();
			file.close();
			
		} catch (json.JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		createIndex(searchKeyword);
	}
	
	public void createIndex(String searchKeyword) throws IOException, ParseException, JSONException{
	 //   String _indexPath = "lucene-paper-index";
	 //   String _prefix = "../../../../../Documents/Research/AutomaticPaperSelector/IR_Base/data/NewData/";

		String _indexPath = System.getenv().get("Index_path");
		String _data_path = System.getenv().get("Data_path");
	    Indexer.index(_indexPath, _data_path);
	//    searchIndex(searchKeyword);
	    
	}
	
	/*Copied from main, learnOnline methods under PaperReco class*/
	public static JSONObject generateRank() throws InvalidFormatException, FileNotFoundException, IOException, JSONException{	//No input as the query is set to "Machine Learning"
		String basePath= "/Users/AustinKim/Documents/Research/AutomaticPaperSelector/IR_Base";
		/*****Set these parameters before run the classifiers.*****/
		int classNumber = 2; //Define the number of classes
		int Ngram = 2; //The default value is bigram. 
		int lengthThreshold = 2; //Document length threshold
		
		String featureValue = "TFIDF"; //The way of calculating the feature value, which can also be "TFIDF", "BM25"
		int norm = 0;//The way of normalization.(only 1 and 2)
		int CVFold = 10; //k fold-cross validation
	

		String style = "SUP";

		String classifier = "SVM"; //Which classifier to use.
		boolean loadModel = true ; //Added by Rizwan loadModel Flag. True to load, false to retrain and save
		boolean cross_validation = false; //Added 
		String model = "NB-EM";
		double C = 1.0;
		
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("Parameters of this run:" + "\nClassNumber: " + classNumber + "\tNgram: " + Ngram + "\tFeatureValue: " + featureValue + "\tLearning Method: " + style + "\tClassifier: " + classifier + "\nCross validation: " + CVFold);

//		/*****Parameters in feature selection.*****/
		String featureSelection = "CHI"; //Feature selection method.
		String stopwords = basePath+"/data/Model/stopwords.dat";
		double startProb = 0.1; // Used in feature selection, the starting point of the features.
		double endProb = 0.999; // Used in feature selection, the ending point of the features.
		int maxDF = -1, minDF = 2; // Filter the features with DFs smaller than this threshold.
		
		/*****The parameters used in loading files.*****/
		String folder = basePath+"/data/WebofScience";
		String suffix = ".json";
		String tokenModel = basePath+"/data/Model/en-token.bin"; //Token model
		
		
		/*** chunk to annotate once *****/
		int chunkSize = 10;
		/** learning rate**/
		double eta = 0.001;
		
		String pattern = String.format("%dgram_%s", Ngram, featureSelection);
		String fvFile = String.format(basePath+"/data/Features/fv_%s_small.txt", pattern);
		String fvStatFile = String.format(basePath+"/data/Features/fv_stat_%s_small.txt", pattern);
		String vctFile = String.format(basePath+"/data/Fvs/vct_%s_tablet_small.dat", pattern);		
		
		/*****Parameters in time series analysis.*****/
		int window = 0;
		System.out.println("Window length: " + window);
		System.out.println("--------------------------------------------------------------------------------------");
		
		DocAnalyzer tempAnalyzer = new DocAnalyzer(tokenModel, classNumber, fvFile, Ngram, lengthThreshold);
		SVM mySVM = new SVM(tempAnalyzer.getCorpus(), C);
		
		SVMRanker myRanker = new SVMRanker(mySVM, basePath+"/data/WebOfScience/WebofScienceModel.txt");

		String newData = basePath+"/data/NewData/";
		tempAnalyzer.LoadDirectory(newData, suffix);
		
		ArrayList<_Doc> docs_list = tempAnalyzer.getCorpus().getCollection();
		
		
		MyPriorityQueue<_RankItem> rankinglist = new MyPriorityQueue<_RankItem>(chunkSize, true);
		ArrayList<_Doc> ranked_list=  new ArrayList<_Doc>();;
		
		for(int i=0; i<docs_list.size(); i++) {
			_Doc tempDoc = docs_list.get(i);
			rankinglist.add(new _RankItem(i, myRanker.getScore(tempDoc)));
		}
		for(_RankItem it : rankinglist){
			_Doc tempDoc = docs_list.get(it.m_index);
			ranked_list.add(tempDoc);
			
			
			System.out.println("index: "+ it.m_index +", ReferaceId: " +tempDoc.getName()+"\nTitle: "+tempDoc.getTitle()+"\nContent: "+tempDoc.getContent()+"\nCurrent Annotation: "+((tempDoc.getYLabel()==1)?"Positive":"Negative"));
			System.out.println("Array Size: "+ ranked_list.size());
//			docsToRemove.add(tempDoc);
		}
		JSONObject info = PaperReco.generateJsonObject(ranked_list,false);
		return info;
		
		
	
	}

}
