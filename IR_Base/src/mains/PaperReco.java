package mains;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Analyzer.DocAnalyzer;
import Classifier.supervised.SVM;
import structures.MyPriorityQueue;
import structures._Doc;
import structures._RankItem;
import Ranker.SVMRanker;
import index.similarities.OkapiBM25;
import index.Indexer;
import index.SearchResult;
import index.Searcher;
import index.ResultDoc;
import crawler.WebOfScienceCrawler;;

public class PaperReco {
	public static JSONObject generateJsonObject(ArrayList<_Doc>docs, boolean change) throws JSONException
	{
		JSONArray annoattedReviews = new JSONArray();
		int index =0;
		while(index<docs.size())
		{
//			System.out.println(index);
			JSONObject obj = new JSONObject();
			obj.put("Author", "");
//			obj.put("ReviewID", String.valueOf(System.currentTimeMillis()));
			if(change){
//				System.out.println("Changing reviewId from: "+ docs.get(index).getName()+" to: "+String.valueOf(index));
				obj.put("ReviewID", String.valueOf(index));
			}
			else {
//				System.out.println("Keeping reviewId: "+ docs.get(index).getName());
				obj.put("ReviewID",docs.get(index).getName());
			}
			if(docs.get(index).getYLabel()==0)obj.put("Overall", "1.0");
			else obj.put("Overall", "2.0");
			
			obj.put("Content",docs.get(index).getContent() );
			obj.put("Title", docs.get(index).getTitle());
			obj.put("Date", "June 17, 2005");
			annoattedReviews.put(obj);
			index++;	
			
		}
		JSONObject ProductInfo = new JSONObject();
		ProductInfo.put("Price", "");
		ProductInfo.put("ProductID", "B00007FHEN");
		ProductInfo.put("Features", "");
		ProductInfo.put("ImgURL", "");
		ProductInfo.put("Name", "");
		
		JSONObject newinfo = new JSONObject();
		newinfo.put("Reviews", annoattedReviews);
		newinfo.put("ProductInfo",ProductInfo);
		return newinfo;
	}
	public static void mergeJsons(String folder, String suffix, ArrayList<_Doc>docs, String output_file) throws JSONException, IOException
	{
		File dir = new File(folder);
		for (File f : dir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(suffix)) {
				f.delete();
			} 
		}
		System.out.println("Calling to merge all training sets");
		JSONObject obj = generateJsonObject(docs,true);
		mergeJsons("./NewData", suffix, docs, output_file);
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(output_file), "UTF-8"));
		writer.write(obj.toString());
		writer.close();	
	}
	
	public static void learnOnline(SVMRanker myRanker, int chunkSize, ArrayList<_Doc> docs, double eta, String newData) throws JSONException, IOException
	{
		ArrayList<_Doc> docsToRemove = new ArrayList<_Doc>();
		
		MyPriorityQueue<_RankItem> rankinglist = new MyPriorityQueue<_RankItem>(chunkSize, true);
		for(int i=0; i<docs.size(); i++) {
			_Doc tempDoc = docs.get(i);
			rankinglist.add(new _RankItem(i, myRanker.getScore(tempDoc)));
		}
		
		for(_RankItem it : rankinglist){
			_Doc tempDoc = docs.get(it.m_index);
			System.out.println("index: "+ it.m_index +", ReferaceId: " +tempDoc.getName()+"\nTitle: "+tempDoc.getTitle()+"\nContent: "+tempDoc.getContent()+"\nCurrent Annotation: "+((tempDoc.getYLabel()==1)?"Positive":"Negative"));//+"\nYour annotation (1 for positive 2 for negative): ");
			docsToRemove.add(tempDoc);
		}
		System.out.println("");
	
		Scanner scanner = new Scanner(System.in);
		ArrayList<_Doc> newDocs = new ArrayList<_Doc>();
		String cc;
		
		
		System.out.println("Among these "+chunkSize+ " docs, positive doc indices (in a line seperated by space) are: ");
		cc = scanner.nextLine();
		String[] positive_index_string  = null;
		if(!cc.equals(""))
		{
			positive_index_string = cc.split(" ");
		}
		
		List<Integer> positiveDocIndices = new ArrayList<Integer>();
		
		System.out.println("Among these "+chunkSize+ " docs, negative doc indices (in a line seperated by space) are: ");
		cc = scanner.nextLine();
		String[] negative_index_string  = null;
		if(!cc.equals(""))
		{
			negative_index_string = cc.split(" ");
		}
		
		
		List<Integer> negativeDocIndices = new ArrayList<Integer>();
		int pos_len = 0;
		if(positive_index_string != null){
			pos_len = positive_index_string.length;
		}
		
		int neg_len = 0;
		if(negative_index_string != null){
			neg_len = negative_index_string.length;
		}
		
		
		System.out.println("positive docs annotaed: " + pos_len+ " negative docs annotaed: " +neg_len);
		
		for (int i =0; i<pos_len;i++){
			System.out.println(Integer.parseInt(positive_index_string[i]));
			positiveDocIndices.add(Integer.parseInt(positive_index_string[i]));
		}
		for (int i =0; i<neg_len;i++){
			System.out.println(Integer.parseInt(negative_index_string[i]));
			negativeDocIndices.add(Integer.parseInt(negative_index_string[i]));
		}
		
		
		if(pos_len!=0){
			
			for(int posIndex: positiveDocIndices){
				_Doc posDoc = docs.get(posIndex);
				for(int negIndex: negativeDocIndices){
					_Doc negDoc = docs.get(negIndex);
					System.out.println("Updating model for posDoc: "+ posDoc.getTitle()+ " negDoc: "+ negDoc.getTitle());
					myRanker.weightUpdate(posDoc, negDoc, eta);
				}
			}	
		}
		else{
			for(int negIndex: negativeDocIndices){
				_Doc negDoc = docs.get(negIndex);
				System.out.println("Updating model for"+ " negDoc: "+ negDoc.getTitle());
				myRanker.weightUpdate( null, negDoc, eta*10);
			}
		}
		
		/** removes all annoatated docs **/
		System.out.println(" all docs size: "+docs.size() );
		docs.removeAll(docsToRemove);
		System.out.println(" Docs size now: "+docs.size());	
		
		System.out.println("Want to continue: ");
		cc = scanner.nextLine();
		if(cc.equals("y") || cc.equals("Y")){
			 /////here is just a prototype
			learnOnline(myRanker,chunkSize, docs, eta, newData);
		}
		else{
			JSONObject info = generateJsonObject(docs,false);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newData), "UTF-8"));
			writer.write(info.toString());
			writer.close();
		}
	}
	
	
	public static void main(String[] args) throws IOException, ParseException, InterruptedException, JSONException, org.json.simple.parser.ParseException, json.JSONException{
//		
		//Added by Rizwan for crawling data from Web of science and generate json formated Reviews from Abstracts
		
//		CrawlerInputReader creawler = new CrawlerInputReader();
//		WebOfScienceCrawler c = new WebOfScienceCrawler("Deep Learning", 9);
//		System.out.println(c.crawl());
		
	//	Set these parameters before run the classifiers
/*		int classNumber = 2; //Define the number of classes
		int Ngram = 2; //The default value is bigram. 
		int lengthThreshold = 2; //Document length threshold
		
		//"TF", "TFIDF", "BM25", "PLN"
		String featureValue = "TFIDF"; //The way of calculating the feature value, which can also be "TFIDF", "BM25"
		int norm = 0;//The way of normalization.(only 1 and 2)
		int CVFold = 10; //k fold-cross validation
	
		//"SUP", "SEMI", "FV", "ASPECT"
		String style = "SUP";
		
		//"NB", "LR", "SVM", "PR"
		String classifier = "SVM"; //Which classifier to use.
		boolean loadModel = true ; //Added by Rizwan loadModel Flag. True to load, false to retrain and save
		boolean cross_validation = false; //Added 
		//"GF", "NB-EM"
		String model = "NB-EM";
		double C = 1.0;
//		String modelPath = "./data/Model/";
		String debugOutput = null; //"data/debug/LR.output";
		
		System.out.println("--------------------------------------------------------------------------------------");
		System.out.println("Parameters of this run:" + "\nClassNumber: " + classNumber + "\tNgram: " + Ngram + "\tFeatureValue: " + featureValue + "\tLearning Method: " + style + "\tClassifier: " + classifier + "\nCross validation: " + CVFold);

//		Parameters in feature selection
		String featureSelection = "CHI"; //Feature selection method.
		String stopwords = "./data/Model/stopwords.dat";
		double startProb = 0.1; // Used in feature selection, the starting point of the features.
		double endProb = 0.999; // Used in feature selection, the ending point of the features.
		int maxDF = -1, minDF = 2; // Filter the features with DFs smaller than this threshold.
//		System.out.println("Feature Seleciton: " + featureSelection + "\tStarting probability: " + startProb + "\tEnding probability:" + endProb);
		
//		The parameters used in loading files
		String folder = "./data/WebofScience";
		String suffix = ".json";
		String tokenModel = "./data/Model/en-token.bin"; //Token model
		
		
	//	chunk to annotate once
		int chunkSize = 5;
		double eta = 0.001;	//learning rat
		
		String pattern = String.format("%dgram_%s", Ngram, featureSelection);
		String fvFile = String.format("data/Features/fv_%s_small.txt", pattern);
		String fvStatFile = String.format("data/Features/fv_stat_%s_small.txt", pattern);
		String vctFile = String.format("data/Fvs/vct_%s_tablet_small.dat", pattern);		
		
	//	Parameters in time series analysis.
		int window = 0;
		System.out.println("Window length: " + window);
		System.out.println("--------------------------------------------------------------------------------------");
		
		DocAnalyzer tempAnalyzer = new DocAnalyzer(tokenModel, classNumber, fvFile, Ngram, lengthThreshold);
		SVM mySVM = new SVM(tempAnalyzer.getCorpus(), C);
		
		SVMRanker myRanker = new SVMRanker(mySVM, "./data/WebOfScience/WebofScienceModel.txt");
*/
		
		/*****Set up Indexer. Added by Sonwoo*****/
	    String _indexPath = "lucene-paper-index";
	    String _prefix = "./data/NewData/";
	    Indexer.index(_indexPath, _prefix);
		
		
	    /*****Added by Sonwoo*****/
	    /*****Do Search- for testing, can be commented out*****/
	    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        System.out.println("Type text to search, blank to quit.");
        System.out.print("> ");
        String keywordInput;
            
		Searcher searcher = new Searcher(_indexPath);
		searcher.setSimilarity(new OkapiBM25());	// Search by BM25 feature for now
        JSONArray annoattedReviews = new JSONArray();
        

        while ((keywordInput = br.readLine()) != null && !keywordInput.equals("")) {
    		SearchResult result = searcher.search(keywordInput, "abstractInfo");	//Search!!
    	    ArrayList<ResultDoc> results = result.getDocs();
            int rank = 1;
            if (results.size() == 0)
                System.out.println("No results found!");
	        for (ResultDoc rdoc : results) {
	        	try {
	    	        JSONObject rankeditem= new JSONObject();
					rankeditem.put("Title", rdoc.title());
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
        }

/*        try {
			jsonObject.put("Reviews", annoattedReviews);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	    
	    
	    
	    
		
		
/*		String newData = "./data/NewData/";
		tempAnalyzer.LoadDirectory(newData, suffix);
		
		ArrayList<_Doc> docs_list = tempAnalyzer.getCorpus().getCollection();	
		String output_file = "./data/NewData/collection.json";
		learnOnline(myRanker,chunkSize, docs_list, eta, output_file);
		myRanker.saveRankerModel("./data/WebOfScience/WebofScienceModel.txt");
*/		
		
	}

}
