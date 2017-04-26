package Ranker;

import Classifier.supervised.SVM;
import opennlp.tools.util.InvalidFormatException;
import structures._Doc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import Analyzer.DocAnalyzer;
import Classifier.supervised.SVM;

public class SVMRanker {
	
	SVM mySVM;
	DocAnalyzer trainsetAnalyzer;
	
	public SVMRanker(String tokenModel, int classNumber, String fvFile, int Ngram, int lengthThreshold, double C, String model_path) throws InvalidFormatException, FileNotFoundException, IOException{
		trainsetAnalyzer = new DocAnalyzer(tokenModel, classNumber, fvFile, Ngram, lengthThreshold);
		mySVM = new SVM(trainsetAnalyzer.getCorpus(), C);
		System.out.println("Start loading Model, wait");
		mySVM.loadModel(new File(model_path));
	}
	public SVMRanker(DocAnalyzer docAnalyzer, double C, String model_path) throws IOException{
		mySVM = new SVM(docAnalyzer.getCorpus(), C);
		System.out.println("Start loading Model, wait");
		mySVM.loadModel(new File(model_path));
	}
	public SVMRanker(SVM tSVM, String model_path) throws IOException{
		mySVM = tSVM;
		System.out.println("Start loading Model, wait");
		mySVM.loadModel(new File(model_path));
		System.out.println("Loading Model completed");
	}
	public SVMRanker(SVM tSVM) throws IOException{
		mySVM = tSVM;
	}
	public void loadRankerModel(String model_path) throws IOException{
		System.out.println("Start loading model to "+ model_path);
		mySVM.loadModel(new File(model_path));
		System.out.println("Loading model completed");
	}
	public void saveRankerModel(String model_path){
		System.out.println("Start saving model to "+ model_path);
		mySVM.saveModel(model_path);
		System.out.println("Saving complete");
	}
	public double getScore(_Doc doc)
	{
		return mySVM.score(doc, 1); // label 1 or 0 both are same here, as for positive one
	}
	public void weightUpdate(_Doc posDoc, _Doc negDoc, double eta)
	{
		mySVM.updateModel(eta, posDoc, negDoc);
//		return true;
	}
	 
	
	
	
}
