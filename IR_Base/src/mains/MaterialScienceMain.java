package mains;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Analyzer.DocAnalyzer;
import Classifier.semisupervised.GaussianFields;
import Classifier.supervised.LogisticRegression;
import Classifier.supervised.NaiveBayes;
import Classifier.supervised.SVM;
import structures.MyPriorityQueue;
import structures._Corpus;
import structures._Doc;
import structures._RankItem;
import utils.Utils;

public class MaterialScienceMain {
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
	
	public static void learnOnline(SVM mySVM, int chunkSize, String tokenModel, int classNumber, int Ngram, int lengthThreshold, String folder, String suffix, String featureValue, int norm, ArrayList<_Doc> docs, String newData, double eta) throws IOException, JSONException
	{
		MyPriorityQueue<_RankItem> rankinglist = new MyPriorityQueue<_RankItem>(chunkSize, true);
		for(int i=0; i<docs.size(); i++) {
			_Doc tempDoc = docs.get(i);
			rankinglist.add(new _RankItem(i, mySVM.score(tempDoc, 1)));
		}
		
		for(_RankItem it : rankinglist){
			_Doc tempDoc = docs.get(it.m_index);
			System.out.println("index: "+ it.m_index +", ReferaceId: " +tempDoc.getName()+"\nTitle: "+tempDoc.getTitle()+"\nContent: "+tempDoc.getContent()+"\nCurrent Annotation: "+((tempDoc.getYLabel()==2)?"Positive":"Negative"));//+"\nYour annotation (1 for positive 2 for negative): ");

		}
		System.out.println("");
	
		Scanner scanner = new Scanner(System.in);
		ArrayList<_Doc> newDocs = new ArrayList<_Doc>();
		String cc;
		
		ArrayList<_Doc> docsToRemove = new ArrayList<_Doc>();
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
					mySVM.updateModel(eta, posDoc, negDoc);
				}
			}
		}
		else{
			for(int negIndex: negativeDocIndices){
				_Doc negDoc = docs.get(negIndex);
				System.out.println("Updating model for"+ " negDoc: "+ negDoc.getTitle());
				mySVM.updateModel(eta*10, null, negDoc);
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
			learnOnline(mySVM, chunkSize, tokenModel, classNumber, Ngram, lengthThreshold, folder, suffix, featureValue, norm, docs, newData, eta);	
		}
		else{
			JSONObject info = generateJsonObject(docs,false);
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newData), "UTF-8"));
			writer.write(info.toString());
			writer.close();
		}
		 
	}

	private static List<Integer> getDocIndex(LinkedHashMap<Integer, Double> sortedMap, ArrayList<_Doc> docs) {
		/****** Dummy method just return the chunk sized highest scored index
		 * should be a strategy
		 * docs is just for printing in console
		 */
		
		List<Integer> keyList = new ArrayList<Integer>(sortedMap.keySet());
		//docs is only for printing the following line
//		System.out.println("docz size: "+ docs.size());
//		System.out.println("sorted map size: "+ sortedMap.size());
//		System.out.println("keylist size: "+ keyList.size());
//		System.out.println("now: "+docs.get(keyList.get(keyList.size()-1)).getName()+" next:"+docs.get(keyList.get(keyList.size()-2)).getName()+" then: "+docs.get(keyList.get(keyList.size()-3)).getName()+" after it: :"+docs.get(keyList.get(keyList.size()-4)).getName());
//		System.out.println("now: "+docs.get(keyList.get(keyList.size()-5)).getName()+" next:"+docs.get(keyList.get(keyList.size()-6)).getName()+" then: "+docs.get(keyList.get(keyList.size()-7)).getName()+" after it: :"+docs.get(keyList.get(keyList.size()-8)).getName());
//		System.out.println("now: "+docs.get(keyList.get(keyList.size()-9)).getName()+" next:"+docs.get(keyList.get(keyList.size()-10)).getName()+" then: "+docs.get(keyList.get(keyList.size()-11)).getName()+" after it: :"+docs.get(keyList.get(keyList.size()-12)).getName());
		//return keyList.get(keyList.size()-1);
		return keyList;
	}
	
	
	public static void main(String[] args) throws IOException, ParseException, InterruptedException, JSONException{
//		
		//Added by Rizwan for crawling data from Web of science and generate json formated Reviews from Abstracts
		
//		CrawlerInputReader creawler = new CrawlerInputReader();

		
		/*****Set these parameters before run the classifiers.*****/
		int classNumber = 2; //Define the number of classes
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

//		/*****Parameters in feature selection.*****/
		String featureSelection = "CHI"; //Feature selection method.
		String stopwords = "./data/Model/stopwords.dat";
		double startProb = 0.1; // Used in feature selection, the starting point of the features.
		double endProb = 0.999; // Used in feature selection, the ending point of the features.
		int maxDF = -1, minDF = 2; // Filter the features with DFs smaller than this threshold.
//		System.out.println("Feature Seleciton: " + featureSelection + "\tStarting probability: " + startProb + "\tEnding probability:" + endProb);
		
		/*****The parameters used in loading files.*****/
		String folder = "./data/WebofScience";
		String suffix = ".json";
		String tokenModel = "./data/Model/en-token.bin"; //Token model
		
		
		/*** chunk to annotate once *****/
		int chunkSize = 5;
		/** learning rate**/
		double eta = 0.001;
		
		String pattern = String.format("%dgram_%s", Ngram, featureSelection);
		String fvFile = String.format("data/Features/fv_%s_small.txt", pattern);
		String fvStatFile = String.format("data/Features/fv_stat_%s_small.txt", pattern);
		String vctFile = String.format("data/Fvs/vct_%s_tablet_small.dat", pattern);		
		
		/*****Parameters in time series analysis.*****/
		int window = 0;
		System.out.println("Window length: " + window);
		System.out.println("--------------------------------------------------------------------------------------");
		
//		/****Loading json files*****/
		DocAnalyzer analyzer = new DocAnalyzer(tokenModel, classNumber, null, Ngram, lengthThreshold);
		
//		/****Feature selection*****/
		if (loadModel==false) { 
			analyzer.LoadStopwords(stopwords);
			analyzer.LoadDirectory(folder, suffix); //Load all the documents as the data set.
			System.out.println("Performing feature selection, wait...");
			analyzer.featureSelection(fvFile, featureSelection, startProb, endProb, maxDF, minDF); //Select the features.
			
			analyzer.SaveCVStat(fvStatFile);	
			
			analyzer = new DocAnalyzer(tokenModel, classNumber, fvFile, Ngram, lengthThreshold);
			analyzer.setReleaseContent( !(classifier.equals("PR") || debugOutput!=null) );//Just for debugging purpose: all the other classifiers do not need content
			analyzer.LoadDirectory(folder, suffix); //Load all the documents as the data set.
			analyzer.setFeatureValues(featureValue, norm);
		}
		
		_Corpus corpus = analyzer.getCorpus();
//		System.out.println("corpus: "+ corpus.getSize());
		
		/********Choose different classification methods.*********/
		//Execute different classifiers.
		if (style.equals("SUP")) {
			if(classifier.equals("NB")){
				//Define a new naive bayes with the parameters.
				System.out.println("Start naive bayes, wait...");
				NaiveBayes myNB = new NaiveBayes(corpus);
				myNB.crossValidation(CVFold, corpus);//Use the movie reviews for testing the codes.
				
			} else if(classifier.equals("LR")){
				//Define a new logistics regression with the parameters.
				System.out.println("Start logistic regression, wait...");
				LogisticRegression myLR = new LogisticRegression(corpus, C);
				myLR.setDebugOutput(debugOutput);
				
				myLR.crossValidation(CVFold, corpus);//Use the movie reviews for testing the codes.
				//myLR.saveModel(modelPath + "LR.model");
			} else if(classifier.equals("SVM")){
				
				
				
				// Added by Rizwan for storing trained SVM model and then load later to predict for new test

				if(loadModel==false){
					SVM mySVM = new SVM(corpus, C);
					System.out.println("Start SVM, wait...");
					if(cross_validation == true ){
						mySVM.crossValidation(CVFold, corpus);
					}
					else{
						System.out.println("Training the whole corpus\n");
						mySVM.trainCorpus(corpus);
						System.out.println("Finished training\n");
					}
					System.out.println("Start saving Model, wait");
					mySVM.saveModel("./data/WebOfScience/WebofScienceModel.txt");
					System.out.println("Saved the Model");
					loadModel = true;
				}
				else{
					DocAnalyzer trainsetAnalyzer = new DocAnalyzer(tokenModel, classNumber, fvFile, Ngram, lengthThreshold);
//					trainsetAnalyzer.LoadDirectory(folder, suffix);
					SVM mySVM = new SVM(trainsetAnalyzer.getCorpus(), C);
					System.out.println("Start loading Model, wait");
					mySVM.loadModel(new File("./data/WebOfScience/WebofScienceModel.txt"));
					System.out.println("Loaded the Model");
					
					
//					String newData = "./data/NewData/2d.txt.json";
					String newData = "./data/NewData/";
					DocAnalyzer tempAnalyzer = new DocAnalyzer(tokenModel, classNumber, fvFile, Ngram, lengthThreshold);
					
//					tempAnalyzer.LoadDoc(newData);
					tempAnalyzer.LoadDirectory(newData, suffix);
					
					ArrayList<_Doc> docs_list = tempAnalyzer.getCorpus().getCollection();
//					File f = new File(newData);
//					f.delete();
					
					
//					double [] weight_t =  mySVM.getWeights();
//					System.out.println(weight_t.length+" "+ weight_t[weight_t.length-1]);
					
					String output_file = "./data/NewData/collection.json";
//					mergeJsons(newData, suffix, docs_list, output_file);
					learnOnline(mySVM, chunkSize, tokenModel, classNumber, Ngram, lengthThreshold, folder, suffix, featureValue, norm, docs_list, output_file, eta);	
					
					
					System.out.println("Saving model parameters");
					mySVM.saveModel("./data/WebOfScience/WebofScienceModel.txt");
					System.out.println("Done saving model parameters");
				}
						
				
			} else System.out.println("Classifier has not developed yet!");
		} else if (style.equals("SEMI")) {
			if (model.equals("GF")) {
				System.out.println("Start Gaussian Field, wait...");
				GaussianFields mySemi = new GaussianFields(corpus, classifier, C);
				mySemi.crossValidation(CVFold, corpus); 
			} 
		} else if (style.equals("FV")) {
			corpus.save2File(vctFile);
			System.out.format("Vectors saved to %s...\n", vctFile);
		} else System.out.println("Learning paradigm has not developed yet!");
	}
}
