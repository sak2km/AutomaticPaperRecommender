package index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class OkapiBM25 extends SimilarityBase {
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats
     *            Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */
    @Override
    protected float score(BasicStats stats, float termFreq, float docLength) {
        double k1 = 1.5;
        double k2 = 750;
        double b = 1.0;
        
        double N = stats.getNumberOfDocuments();
        double df = stats.getDocFreq();
        
        double nAvg = stats.getAvgFieldLength();
        
        double term1 = Math.log((N-df+0.5)/(df+0.5));
        double term2 = ((k1+1)*termFreq)/(k1*(1-b+b*docLength/nAvg)+termFreq);
        double term3 = (k2+1)/(k2+1);
        double score = term1*term2*term3;
    	
    	
    	
    	return (float)score;
    }

    @Override
    public String toString() {
        return "Okapi BM25";
    }

}