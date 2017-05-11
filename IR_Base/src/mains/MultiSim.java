package mains;

import org.apache.lucene.index.AtomicReaderContext;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yiling on 4/27/17.
 */
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class MultiSim extends Similarity {
    protected final Similarity[] sims;
    SimScorer[] subScorers;

    public MultiSim(Similarity[] sims) {
        this.sims = sims;
    }

    public long computeNorm(FieldInvertState state) {
        return this.sims[0].computeNorm(state);
    }

    public SimWeight computeWeight(float queryBoost, CollectionStatistics collectionStats, TermStatistics... termStats) {
        SimWeight[] subStats = new SimWeight[this.sims.length];

        for(int i = 0; i < subStats.length; ++i) {
            subStats[i] = this.sims[i].computeWeight(queryBoost, collectionStats, termStats);
        }

        return new MultiSim.MultiStats(subStats);
    }

    public SimScorer simScorer(SimWeight stats, AtomicReaderContext context) throws IOException {
        subScorers = new SimScorer[this.sims.length];

        for(int i = 0; i < subScorers.length; ++i) {
            subScorers[i] = this.sims[i].simScorer(((MultiSim.MultiStats)stats).subStats[i], context);
        }

        return new MultiSim.MultiSimScorer(subScorers);
    }



    public class MultiStats extends SimWeight {
        final SimWeight[] subStats;

        MultiStats(SimWeight[] subStats) {
            this.subStats = subStats;
        }

        public float getValueForNormalization() {
            float sum = 0.0F;
            SimWeight[] arr$ = this.subStats;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                SimWeight stat = arr$[i$];
                sum += stat.getValueForNormalization();
            }

            return sum / (float)this.subStats.length;
        }

        public void normalize(float queryNorm, float topLevelBoost) {
            SimWeight[] arr$ = this.subStats;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                SimWeight stat = arr$[i$];
                stat.normalize(queryNorm, topLevelBoost);
            }

        }
    }

    public class MultiSimScorer extends SimScorer {
        private final SimScorer[] subScorers;

        MultiSimScorer(SimScorer[] subScorers) {
            this.subScorers = subScorers;
        }

        public float score(int doc, float freq) {

            float sum = 0.0F;
            SimScorer[] arr$ = this.subScorers;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                SimScorer subScorer = arr$[i$];
                sum += subScorer.score(doc, freq);
                if (doc == 158){}
   //                 System.out.print("Round " + L2RFeature.indexbase + " Doc ID: " + doc + " " + subScorer.score(doc, freq) + "\n");
//                L2RFeature.test = 1;
                if ((L2RFeature.DocIndex.get(doc)) != null) {
                    L2RFeature.feature[L2RFeature.DocIndex.get(doc)][i$ + 6 * L2RFeature.indexbase] += subScorer.score(doc, freq);
//                    if(L2RFeature.indexbase == 5){
//                        L2RFeature.feature[L2RFeature.DocIndex.get(doc)][36] = subScorer.
//                    }
                }
            }
            return sum;
        }

        public Explanation explain(int doc, Explanation freq) {
            Explanation expl = new Explanation(this.score(doc, freq.getValue()), "sum of:");
            SimScorer[] arr$ = this.subScorers;
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
                SimScorer subScorer = arr$[i$];
                expl.addDetail(subScorer.explain(doc, freq));
            }

            return expl;
        }

        public float computeSlopFactor(int distance) {
            return this.subScorers[0].computeSlopFactor(distance);
        }

        public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
            return this.subScorers[0].computePayloadFactor(doc, start, end, payload);
        }
    }
}