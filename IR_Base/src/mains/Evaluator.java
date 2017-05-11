package mains;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.FSDirectory;

public class Evaluator {
	String[] relevantDocs;
	static L2RWeight l2RWeight;
	private static final String _judgeFile = "../annotation.txt";
	
	public Evaluator( L2RWeight weight) {
		l2RWeight = weight;
    }
	
	
	public void evaluate() throws IOException, ParseException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new FileReader(_judgeFile));
		String query = null, judgement = null;
		int k = 30;
		double meanAvgPrec = 0.0, p_k = 0.0, mRR = 0.0, nDCG = 0.0;
		double numQueries = 0.0;

		while ((query = br.readLine()) != null) {
			judgement = br.readLine();
			judgement = judgement.replaceAll("WOS:","");
			HashSet<String> relDocs = new HashSet<String>(Arrays.asList(judgement.split("\t")));
			l2RWeight.ExtractFeature(query);
	        l2RWeight.ConstructList();	// Makes a list of top-N documents with largest score
			
			/*	convert relevant doc table of WOS String to int of Lucene DocID	*/
	        HashSet<Integer> reldoc_Integer = new HashSet<Integer>(relDocs.size());
			for(String rdoc : relDocs){
				reldoc_Integer.add(l2RWeight.WosDocId.get(rdoc));
			}

			mRR += RR(query,reldoc_Integer);
			meanAvgPrec += AvgPrec(query,reldoc_Integer);			
			nDCG += NDCG(query,reldoc_Integer, k);
			p_k += Prec(query,reldoc_Integer, k);

			
			++numQueries;
		}
		br.close();
		System.out.println();
		System.out.println("\nMAP: " + meanAvgPrec / numQueries);
		System.out.println("\nNDCG: " + nDCG / numQueries);
		System.out.println("\nP@" + k + ": " + p_k / numQueries);
		System.out.println("\nMRR: " + mRR / numQueries);

	}
	
	public void evaluate_demo(int i,PrintWriter writer_map,PrintWriter writer_ndcg,PrintWriter writer_rr,PrintWriter writer_precK ) throws IOException, ParseException, ClassNotFoundException {
		// TODO Auto-generated method stub
		BufferedReader br = new BufferedReader(new FileReader(_judgeFile));
		String query = null, judgement = null;
		int k = 30;
		double meanAvgPrec = 0.0, p_k = 0.0, mRR = 0.0, nDCG = 0.0;
		double numQueries = 0.0;
		String[] clicked_list,displayed_list;
			
//		System.out.println("////////////////////////////");

		while ((query = br.readLine()) != null) {
			l2RWeight.ExtractFeature(query);
	        l2RWeight.ConstructList();	// Makes a list of top-N documents with largest score
	        

	        displayed_list = new String[l2RWeight.I.length];
	        
	        /*	Create relevant doc table of WOS String to Lucene DocID (int)	*/
			judgement = br.readLine();
			judgement = judgement.replaceAll("WOS:","");
			HashSet<String> relDocs = new HashSet<String>(Arrays.asList(judgement.split("\t")));
	        HashSet<Integer> reldoc_Integer = new HashSet<Integer>(relDocs.size());
	        
			clicked_list = new String[relDocs.size()];
			int p=0;
			for(String rdoc : relDocs){
				Integer docId = l2RWeight.WosDocId.get(rdoc);
				reldoc_Integer.add(docId);
				if(p<relDocs.size() && docId!=null)
					clicked_list[p]=docId.toString();	 // clicked array
				p++;
			}
			
			if(i>0){	// if not initial evaluation, update weights
				/*	Generate clickthrough data	*/				
				for(int q=0;q<l2RWeight.I.length;q++){
					displayed_list[q] =l2RWeight.I[q].toString();	//displayed array
				}	
				String [][] rankedPair = ScoreCalculator.generatePairs(displayed_list,clicked_list);
				
				/*Create Int version of rankedPair[]*/
				Integer[][] rankedPair_int = new Integer[rankedPair.length][2];
				for(int t=0;t<rankedPair.length;t++){
					for(int j=0;j<2;j++){
						rankedPair_int[t][j]= Integer.parseInt(rankedPair[t][j]);
					}
				}
		        l2RWeight.update(rankedPair_int);
		        l2RWeight.ConstructList();		// get new ranking with new weight        
			}
			
			
			/*	Calculate evaluation metrics	*/
			mRR += RR(query,reldoc_Integer);
			meanAvgPrec += AvgPrec(query,reldoc_Integer);			
			nDCG += NDCG(query,reldoc_Integer, k);
			p_k += Prec(query,reldoc_Integer, k);

			
			++numQueries;
		}
		br.close();
	//	ScoreCalculator.saveWeight(l2RWeight.weight); // save weights on DB	(only for recording purpose).
		// The same l2RWeight class retains its weight value for every evaluation query.
		System.out.println("\nMAP: " + meanAvgPrec / numQueries);
		System.out.println("\nNDCG: " + nDCG / numQueries);
		System.out.println("\nP@" + k + ": " + p_k / numQueries);
		System.out.println("\nMRR: " + mRR / numQueries);
		System.out.println("weight[0]= "+l2RWeight.weight[0]);
		if(i%100==0){
			System.out.println("Iteration "+i+":");
		//    writer.println("=============================================================");
//		    writer.println("Iteration: "+i);
//		    writer_map.println(meanAvgPrec / numQueries);
//		    writer_ndcg.println(nDCG / numQueries);
//		    writer_precK.println( p_k / numQueries);
//		    writer_rr.println(mRR / numQueries);
//		    writer.println("weight (first 9)= "+l2RWeight.weight[0]+" "+l2RWeight.weight[1]+" "+l2RWeight.weight[2]+" "+l2RWeight.weight[3]+" "+l2RWeight.weight[4]+" "+l2RWeight.weight[5]+" "+l2RWeight.weight[6]+" "+l2RWeight.weight[7]+" "+l2RWeight.weight[8]+" ");
		}
		
		

	}
	//precision at K
	private static double Prec(String query, HashSet<Integer> reldoc_Integer, int k) {
		double p_k = 0;
		if (reldoc_Integer.size() == 0)
			return 0; // no result returned
		int i = 1;
		double numRel = 0;
//		System.out.println("\nQuery: " + query);
		for (Integer rdoc : l2RWeight.I) {
			if(i<=k) {
				if (reldoc_Integer.contains(rdoc)) {	// when we encounter a relevant document
					numRel ++;
			//		System.out.print("  ");
				} else {	//when we encounter an irrelevant document
			//		System.out.print("X ");
				}
			//	System.out.println(i + ". " + rdoc);
				++i;				
			}
		}
		p_k = numRel/k;
		
//		System.out.println("Precision @"+ k +": " + p_k);
		return p_k;
	}
	
	//Reciprocal Rank
	private static double RR(String query, HashSet<Integer> reldoc_Integer) {
		double rr = 0;
		boolean foundRel = false;
		int k = 0;

		int i = 1;
//		System.out.println("\nQuery: " + query);
		for (Integer rdoc : l2RWeight.I) {
			if (reldoc_Integer.contains(rdoc)) {	// when we encounter a relevant document
				if(!foundRel){
					k = i;
					foundRel = true;
	//				System.out.print("  ");
	//				System.out.println(i + ". " + rdoc);
					break;
				}
			} else {	//when we encounter an irrelevant document
	//			System.out.print("X ");
	//			System.out.println(i + ". " + rdoc);
			}
			++i;	
		}
		if(k>0)
			rr = 1.0/k;
		else 
			rr = 0;	
//		System.out.println("Reciporal Rank: " + rr);
		return rr;
	}
	
	//Normalized Discounted Cumulative Gain
	private static double NDCG(String query, HashSet<Integer> reldoc_Integer, int k) {
		double ndcg = 0;
		double dcg = 0;
		double dcg_GT = 0;

		int i = 1;
		for (Integer rdoc : l2RWeight.I) {
			if(i<=k) {
				dcg_GT+= 1/(Math.log(1+i)/Math.log(2));	// accumulate DCG of ground truth
				if (reldoc_Integer.contains(rdoc)) {	// when we encounter a relevant document
					//use the identity logb(n) = loge(n) / loge(b) to get log base 2
					dcg += 1/(Math.log(1+i)/Math.log(2));	
//					System.out.print("  ");
				} else {	//when we encounter an irrelevant document
//					System.out.print("X ");
				}
//				System.out.println(i + ". " + rdoc);
				++i;				
			}
			else break;
		}
		ndcg = dcg/dcg_GT;	//normalize by dividing by DCG of ground truth
		
//		System.out.println("NDCG @"+ k +": " + ndcg);
		return ndcg;
	}
	private static double AvgPrec(String query, HashSet<Integer> reldoc_Integer) {				
		int i = 1;
		double avgp = 0.0;
		double numRel = 0;
		double sumPrecision = 0;
//		System.out.println("\nQuery: " + query);
		double reldoc_adj = reldoc_Integer.size();
		if (reldoc_Integer.size()>0){
			reldoc_adj = reldoc_Integer.size()-1;
		}
		for (Integer rdoc : l2RWeight.I) {
			if (reldoc_Integer.contains(rdoc)) {
				//how to accumulate average precision (avgp) when we encounter a relevant document
				numRel ++;
				sumPrecision += numRel/i;
//				System.out.println("Match position: "+i);
	//			System.out.println("Precision: "+numRel/i);
	//			System.out.print("  ");
			} else {
				//how to accumulate average precision (avgp) when we encounter an irrelevant document
	//			System.out.print("X ");
			}
	//		System.out.println(i + ". " + rdoc.title());
			++i;
			if(numRel == reldoc_adj) {
				break;
			}
		}
		//compute average precision here
		if(reldoc_adj>0)	//only if at least 1 relevant document found. Else, return 0
			avgp = sumPrecision/reldoc_adj;
	//	System.out.println("Average Precision: " + avgp);
		return avgp;
	}
	
	
	public static void main(String[] args) throws ClassNotFoundException, IOException, ParseException {
		String _indexPath = System.getenv().get("Index_path");
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(_indexPath)));
        L2RWeight l2RWeight = new L2RWeight(reader);
        Evaluator evaluator = new Evaluator(l2RWeight);

	    PrintWriter writer_ndcg = new PrintWriter("Evaluation Result_ndcg.txt", "UTF-8");
	    PrintWriter writer_rr = new PrintWriter("Evaluation Result_rr.txt", "UTF-8");
	    PrintWriter writer_precK = new PrintWriter("Evaluation Result_precK.txt", "UTF-8");
	    PrintWriter writer_map = new PrintWriter("Evaluation Result_map.txt", "UTF-8");
        for(int i=0;i<30;i++){
        	evaluator.evaluate_demo(i,writer_map,writer_ndcg,writer_rr,writer_precK);
        	
        	
        }
        writer_precK.close();
        writer_rr.close();
        writer_ndcg.close();
        writer_map.close();
     //   evaluator.evaluate();
		
	}
	 
	

}
