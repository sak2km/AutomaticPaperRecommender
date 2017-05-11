package mains;

import EDU.oswego.cs.dl.util.concurrent.FJTask;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;

/**
 * Created by Yiling on 4/27/17.
 */
public class L2RWeight {
    int numofFeature = 42;
    public double[] weight = new double[numofFeature];
    IndexReader reader;
    double[][] X;
    double[] S;
    Integer[] L;
    public Integer[] I;

    static Hashtable<Integer, Integer> DocIndex;
    static Hashtable<Integer, Integer> InverseDocIndex;
    static Hashtable<String, Integer> WosDocId = new Hashtable<String, Integer>();
    static Hashtable<Integer, String> WosDocId_inverse = new Hashtable<Integer, String>();


    public L2RWeight(IndexReader r) throws IOException, ParseException, ClassNotFoundException{
    	weight=ScoreCalculator.loadWeight(numofFeature);
        reader = r;
        WosHashtable();
        WosHashtable_inverse();
    }

    public void ExtractFeature(String q) throws IOException, ParseException{
        L2RFeature f = new L2RFeature(reader);
        f.GenerateFeature(q);
        X = f.GetFeature();
        DocIndex = f.getDocIndex();
        InverseDocIndex = f.getInverseDocIndex();
    }

    public void ConstructList(){
        int size = X.length;
        S = new double[size];
        double score;
        for (int i = 0; i < size; i++){
            score = 0;
            for (int j = 0; j < numofFeature; j++){
                score += X[i][j] * weight[j];
            }
            S[i] = score;
        }
        sortDescendingbyScore();
//        DisplayI(20);
    }

    public void sortDescendingbyScore(){
        L = new Integer[X.length];
        for(int i = 0; i < X.length; i++)
            L[i] = i;
        Arrays.sort(L, new Comparator<Integer>(){
            @Override public int compare(final Integer o1, final Integer o2){
                return Double.compare(S[o2], S[o1]);
            }
        });
        I = new Integer[X.length];
        for (int i = 0; i < X.length; i++){
            I[i] = InverseDocIndex.get(L[i]);
        }
    }

    public void DisplayI(int k){
         // show first 10
        System.out.print("The Document Index of top- " + k + " results\n");
        for (int i = 0; i < k; i++){
            System.out.print(i+1 + ") " + I[i] +  " " + S[L[i]] + '\n');
        }
    }

    public void subupdate(Integer a_i, Integer b_i, double y_i, double eta, double lambda){
        double[] X_ai = X[a_i];
        double[] X_bi = X[b_i];
        double loss = 0;
        for(int  i = 0; i < X_ai.length; i++){
            loss += (X_ai[i] - X_bi[i]) * weight[i];
        }
        loss *= y_i;
  //      System.out.print(a_i + " " + b_i + " " + loss + "\n");
        if(loss < 1.0 && y_i != 0){
            for(int k = 0; k < weight.length; k++){
                weight[k] = weight[k] + eta * y_i * (X_ai[k] - X_bi[k]) - eta * lambda * weight[k];
            }
        }
    }

    public void update(Integer[][] pairs){
        int size = pairs.length;
        int a_i,b_i;
        for(int i = 0; i < size; i++){
        	if(DocIndex.get(pairs[i][0])!=null && DocIndex.get(pairs[i][1])!=null){
                a_i = DocIndex.get(pairs[i][0]);
                b_i = DocIndex.get(pairs[i][1]); 
                subupdate(a_i, b_i, 1, 0.005, 0);      		
        	}
        }
    }

    public void update(String[][] pairs){
        int size = pairs.length;
        Integer[][] Docpairs = new Integer[size][2];
        for(int i = 0; i < size; i++){
            Docpairs[i][0] = WosDocId.get(pairs[i][0]);
            Docpairs[i][1] = WosDocId.get(pairs[i][1]);
        }
        update(Docpairs);
    }

    public void test(Integer k){
        int index = DocIndex.get(k);
//        double sum = 0;
        for(int  i = 0; i < 20; i++){
            System.out.print(X[index][i] + " ");
        }
        System.out.print("\n" + S[index]);
    }

    public void printweight(){
        System.out.print("Weights:\n");
        for(int i = 0; i < weight.length; i++)
            System.out.print(weight[i] + " ");
        System.out.print("\n");
    }
    public void setWeight() throws ClassNotFoundException{
    	weight=ScoreCalculator.loadWeight(numofFeature);
    }

    public void WosHashtable() throws IOException, ParseException{
        int size = reader.maxDoc();
        Document doc;
        String docInfo, Wos;
        int pos;

        for(Integer i = 0; i < size; i++){
            doc = reader.document(i);
            docInfo = doc.getField("documentInfo").stringValue();
            if(!docInfo.equals("")){
      //          System.out.print(i + " " + docInfo + "\n");
                pos = docInfo.indexOf("WOS") + 4;
     //           System.out.print(docInfo + " " + pos + "\n");
                Wos = docInfo.substring(pos, pos + 15);
                WosDocId.put(Wos, i);
            }
        }
    }
    public void WosHashtable_inverse() throws IOException, ParseException{
        int size = reader.maxDoc();
        Document doc;
        String wos;
        int pos;

        for(Integer i = 0; i < size; i++){
            doc = reader.document(i);
            wos = doc.getField("wos").stringValue();
            if(!wos.equals("")){
                WosDocId_inverse.put(i,wos);
            }
        }
    }

    public static void main(String[] args) throws IOException, ParseException, ClassNotFoundException{
        String path = "./data/lucene-paper-index";
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(path)));
        L2RWeight l2RWeight = new L2RWeight(reader);
//        Analyzer analyzer = new index.SpecialAnalyzer();
//
//        String query = "information retrieval";
//
//        l2RWeight.ExtractFeature(query);
//        l2RWeight.ConstructList();
//        l2RWeight.printweight();
    }
}
