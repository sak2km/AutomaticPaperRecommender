package mains;
//package index;

//import com.sun.java.util.jar.pack.ConstantPool;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.Query;
import index.SpecialAnalyzer;
import org.jsoup.select.Evaluator;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

/**
 * Created by yj9xs on 4/14/17.
 */
public class L2RFeature {


//    static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);

    final IndexReader reader;
    final IndexSearcher searcher;
    static int numDocs;
    static int numHits;
    static Analyzer analyzer = new index.SpecialAnalyzer();
    static double[][] feature;
    static int NumofFeature;
    static int NumofSim = 20;
    static int NumofNotSimFeature = 0;
    Hashtable<Integer, Integer> DocIndex;
    Similarity[] sims;

    public L2RFeature(IndexReader r) {
        reader = r;
        searcher = new IndexSearcher(r);
        numDocs = r.numDocs();
        NumofFeature = NumofSim + NumofNotSimFeature;
        SetSims();
    }

    public void SetSims(){
        sims = new Similarity[NumofSim];
        int index = NumofNotSimFeature;

        sims[index++] = new DefaultSimilarity();
        sims[index++] = new BM25Similarity((float)1.2, (float)0.75);
        // DFRSimilairity: 7*3*5
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffectL(), new NormalizationH1());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffectL(), new NormalizationH2());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffectL(), new NormalizationH3());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffectL(), new NormalizationZ());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffectB(), new NormalizationH1());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffectB(), new NormalizationH2());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffectB(), new NormalizationH3());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffectB(), new NormalizationZ());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffect.NoAfterEffect(), new NormalizationH1());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffect.NoAfterEffect(), new NormalizationH2());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffect.NoAfterEffect(), new NormalizationH3());
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffect.NoAfterEffect(), new NormalizationZ());
        // IBSimilarity: 2*2*5
        sims[index++] = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH1());
        sims[index++] = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH2());
        sims[index++] = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH3());
        sims[index++] = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationZ());
        // LMSimilarity
        sims[index++] = new LMDirichletSimilarity();
        sims[index++] = new LMJelinekMercerSimilarity((float)0.7);
    }

    public void setSimilarity(Similarity sim){searcher.setSimilarity(sim);}

    public Similarity getSimilarity(){return searcher.getSimilarity();}

    public ScoreDoc[] Query(Query q) throws IOException {
        TopDocs docs = searcher.search(q, numDocs);
        ScoreDoc[] hits = docs.scoreDocs;
        return hits;
    }

    public ScoreDoc[] Query(String query, Similarity sim) throws IOException, ParseException {
        searcher.setSimilarity(sim);
        Query luceneQuery = new QueryParser(Version.LUCENE_46, "content", analyzer).parse(query);
        return Query(luceneQuery);
    }

    public Hashtable<Integer, Integer> CreateDocIndex(ScoreDoc[] hits){

        Hashtable<Integer, Integer> DocIndex = new Hashtable<Integer, Integer>();
        int size = hits.length;
        int DocId;
        for (int i = 0; i < size; i++){
            DocId = hits[i].doc;
            DocIndex.put(DocId, i);
        }
        return DocIndex;
    }

    public double[][] UpdateFeature(ScoreDoc[] hits, double[][] feature, int index){

        int featureidx;
        for (int i = 0; i < hits.length; i++){
            featureidx = DocIndex.get(hits[i].doc);
            feature[featureidx][index] = hits[i].score;
        }
        return feature;
    }

//    public double[][] GetNoSimFeature(ScoreDoc[] hits, double[][] feature){
//        int featureidx;
//        for (*)
//    }
    public void GenerateFeature(String query) throws IOException, ParseException{

        ScoreDoc[] hits;

        // default similarity
        hits = Query(query, sims[1]);
        DocIndex = CreateDocIndex(hits);
        numHits = hits.length;
        feature = new double[numHits][NumofFeature];

//        feature = GetNoSimFeature(hits, feature);

        feature = UpdateFeature(hits, feature, NumofNotSimFeature);

        for (int i = NumofNotSimFeature; i < NumofFeature; i++){
            hits = Query(query, sims[i]);
            feature = UpdateFeature(hits, feature, i);
        }
    }

    public double[] GetFeatureVector(int docid){

        if (DocIndex.containsKey(docid))
            return feature[DocIndex.get(docid)];
        else
            System.out.println("Document " + docid + " is not hit.\n");
            return feature[0];
    }

    public static void main(String[] args) throws IOException, ParseException{


        String path = "lucene-paper-index";

		String _indexPath = System.getenv().get("Index_path");
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(_indexPath)));
        L2RFeature feature = new L2RFeature(reader);


        String query;
        int docid;
        double[] FeatureVec;


        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter the query: ");
        query = input.readLine();

        while (!query.equals("Exit")) {
            System.out.print("Query: " + query + "\n");
            feature.GenerateFeature(query);
            System.out.println(feature.numHits + " Documents are hit.\n");


            System.out.print("Enter the document ID (Enter -1 to begin a new query): ");
//            docid = scanner.nextInt();
            docid = Integer.parseInt(input.readLine());
            while (docid >= 0) {
                if (!feature.DocIndex.containsKey(docid))
                    System.out.print("Document " + docid + " is not hit.\n");
                else {
                    FeatureVec = feature.GetFeatureVector(docid);
                    System.out.print("Feature Vector for document " + docid + ":\n");
                    for (int i = 0; i < feature.NumofFeature; i++)
                        System.out.print(FeatureVec[i] + " ");
                    System.out.print("\n");
                }
                System.out.print("Enter the document ID (Enter -1 to begin a new query): ");
                docid = Integer.parseInt(input.readLine());
            }
            System.out.print("\n----------------\n");
            System.out.print("Enter the query: ");
            query = input.readLine();
        }


    }
}
