/**
 * 2013-3-29
 * 
 * xuhongfeng
 */
package hongfeng.xu.rec.mahout.hadoop.threshold;

import hongfeng.xu.rec.mahout.hadoop.HadoopHelper;
import hongfeng.xu.rec.mahout.hadoop.matrix.MatrixReducer;
import hongfeng.xu.rec.mahout.hadoop.matrix.VectorCache;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.mahout.math.Vector;

/**
 * @author xuhongfeng
 *
 */
public class MultiplyThresholdMatrixJob extends BaseThreshldMatrixJob {
    private final Path averageSimilarityPath;
    
    public MultiplyThresholdMatrixJob(int n1, int n2, int n3,
            Path multiplyerPath, int threshold, Path averageSimilarityPath) {
        super(n1, n2, n3, multiplyerPath, threshold);
        this.averageSimilarityPath = averageSimilarityPath;
    }
    
    @Override
    protected void initConf(Configuration conf) {
        super.initConf(conf);
        conf.set("averageSimilarityPath", averageSimilarityPath.toString());
    }

    @Override
    protected Class<? extends MatrixReducer> getMatrixReducer() {
        return MyReducer.class;
    }

    public static class MyReducer extends MatrixReducer {
        private VectorCache averageCache;
        private Path averageSimilarityPath;
        private int threshold;
        
        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            super.setup(context);
            averageSimilarityPath = new Path(conf.get("averageSimilarityPath"));
            int vectorCount = conf.getInt("n1", 0);
            int vectorSize = vectorCount;
            threshold = conf.getInt("threshold", 0);
            averageCache = VectorCache.create(vectorCount, vectorSize,
                    averageSimilarityPath, conf);
        }

        public MyReducer() {
            super();
        }
        
        @Override
        protected double calculate(int i, int j, Vector vector1, Vector vector2) {
            if (i == j) {
                return 0.0;
            }
            int n = HadoopHelper.intersect(vector1, vector2);
            if (n >= threshold) {
                double sim = HadoopHelper.cosineSimilarity(vector1, vector2);
                if (sim != 0.0) {
                    return sim;
                }
            }
            Vector v = averageCache.get(i);
            return v.getQuick(j);
        }
    }
}
