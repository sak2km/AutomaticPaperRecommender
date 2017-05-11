package mains;

//import com.sun.java.util.jar.pack.ConstantPool;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.QueryTermExtractor;
import org.apache.lucene.search.highlight.WeightedTerm;
import org.apache.lucene.search.similarities.*;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;
import index.SpecialAnalyzer;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Created by Yiling on 4/14/17.
 */
public class L2RFeature {


//    static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_46);

    final IndexReader reader;
    final IndexSearcher searcher;
    static int numDocs;
    static int numHits = 0;
    static Analyzer analyzer = new index.SpecialAnalyzer();
    static double[][] feature;
//    static double[][] testfeature;
//    static List<List<Double>> newfeature = new ArrayList<>();
    static int NumofFeature;
    static int NumofSim = 6;
    static int NumofNotSimFeature = 0;
    static int test  = 0;
    static int indexbase = 0;
    static Hashtable<Integer, Integer> DocIndex;
    static Hashtable<Integer, Integer> InverseDocIndex;
    Similarity[] sims;
    Similarity similarity;

    public L2RFeature(IndexReader r) {
        reader = r;
        searcher = new IndexSearcher(r);
        numDocs = r.numDocs();
        NumofFeature = NumofSim * 7 + NumofNotSimFeature;
        SetSimilarity();
    }

    public void SetSimilarity(){
        sims = new Similarity[NumofSim];
        int index = 0;

        sims[index++] = new DefaultSimilarity();
        sims[index++] = new BM25Similarity((float)1.2, (float)0.75);
        sims[index++] = new LMDirichletSimilarity();
        sims[index++] = new LMJelinekMercerSimilarity((float)0.7);
        sims[index++] = new DFRSimilarity(new BasicModelBE(), new AfterEffectL(), new NormalizationH1());
        sims[index] = new IBSimilarity(new DistributionLL(), new LambdaDF(), new NormalizationH1());
        similarity = new MultiSim(sims);
    }

    public double[][] GetFeature(){
        return feature;
    }

    public Hashtable<Integer, Integer> getDocIndex(){return DocIndex;}  //(docid, index)
    public Hashtable<Integer, Integer> getInverseDocIndex(){return InverseDocIndex;} //(index, docid)

    public void CreateDocIndex(ScoreDoc[] hits){

        DocIndex = new Hashtable<Integer, Integer>();
        InverseDocIndex = new Hashtable<>();
        int size = hits.length;
        int DocId;
        for (int i = 0; i < size; i++){
            DocId = hits[i].doc;
            DocIndex.put(DocId, i);
            InverseDocIndex.put(i, DocId);
        }
    }

    public void printfeature(){
        for (int i = 0; i < NumofFeature; i++){
            System.out.print(feature[0][i] + " ");
        }
        System.out.print("\n\n");
    }
    public void GenerateFeature(String query) throws IOException, ParseException{
    	indexbase = 0;
        String[] fields = {"abstractInfo", "authors", "title", "journalInfo", "documentInfo", "categories"};
        Query q = new MultiFieldQueryParser(Version.LUCENE_46, fields, analyzer).parse(query);

        TopDocs docs = searcher.search(q, numDocs);
        ScoreDoc[] hits = docs.scoreDocs;
        
        for(ScoreDoc hit : hits){
            Document doc = searcher.doc(hit.doc);
            String contents = doc.getField("title").stringValue();
            String wos = doc.getField("wos").stringValue(); //Added by sak2km, adds wos# as a field in resultDoc
  //          System.out.println(contents);
   //         System.out.println(wos);
        }

        // default similarity
        CreateDocIndex(hits);
        numHits = hits.length;
        feature = new double[numHits][NumofFeature];

        searcher.setSimilarity(similarity);
        docs = searcher.search(q, numDocs);
        hits = docs.scoreDocs;
    //    printfeature();

//        System.out.print(searcher.explain(q, getInverseDocIndex().get(0)));

        for (int i = 0; i < 6; i++){
            indexbase++;
            q = new QueryParser(Version.LUCENE_46, fields[i], analyzer).parse(query);
            searcher.search(q, numDocs);
   //         printfeature();
        }
    }

    public double[] GetFeatureVector(int docid){
        int Idx = DocIndex.get(docid);
        return feature[Idx];
    }

    public static void main(String[] args) throws IOException, ParseException{


//        String path = "./data/lucene-paper-index";
//        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(path)));
        String path = "./data/lucene-paper-index";
        String _indexPath = System.getenv().get("Index_path");
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(_indexPath)));

        
    	L2RFeature f = new L2RFeature(reader);
        f.GenerateFeature("machine learning");




    }
}