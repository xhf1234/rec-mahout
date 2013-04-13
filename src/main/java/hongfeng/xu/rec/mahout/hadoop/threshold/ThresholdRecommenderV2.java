/**
 * 2013-3-29
 * 
 * xuhongfeng
 */
package hongfeng.xu.rec.mahout.hadoop.threshold;

import hongfeng.xu.rec.mahout.config.DataSetConfig;
import hongfeng.xu.rec.mahout.hadoop.matrix.MultiplyMatrixJob;
import hongfeng.xu.rec.mahout.hadoop.matrix.MultiplyNearestNeighborJob;
import hongfeng.xu.rec.mahout.hadoop.recommender.BaseRecommender;
import hongfeng.xu.rec.mahout.hadoop.similarity.ThresholdCosineSimilarityJob;

import org.apache.hadoop.fs.Path;

/**
 * @author xuhongfeng
 *
 */
public class ThresholdRecommenderV2 extends BaseRecommender {
    private final int threshold, k;
    
    public ThresholdRecommenderV2(int threshold, int k) {
        super();
        this.threshold = threshold;
        this.k = k;
    }

    protected int innerRun() throws Exception {
        calculateThresholdSimilarity();
        
        calculateAllocateMatrix();
        
        multiplyAllocateMatrix();
        
        doAllocate();
        
        calculateUUThreshold();
        
        calculateUIThreshold();
        
        recommend(DataSetConfig.getV2UUUIThresholdPath(threshold));
        
        return 0;
    }
    
    private void calculateThresholdSimilarity() throws Exception {
        Path multiplyerPath = DataSetConfig.getUserItemVectorPath();
        ThresholdCosineSimilarityJob thresholdCosineSimilarityJob =
                new ThresholdCosineSimilarityJob(userCount(), itemCount(), userCount(),
                        multiplyerPath, threshold);
        runJob(thresholdCosineSimilarityJob, DataSetConfig.getUserItemVectorPath(),
                DataSetConfig.getUserSimilarityThresholdPath(threshold), true);
    }
    
    private void calculateAllocateMatrix() throws Exception {
        Path similarityVectorPath = new Path(DataSetConfig.getUserSimilarityThresholdPath(threshold),
                "rowVector");
        AllocateMatrixJob allocateMatrixJob = new AllocateMatrixJob(userCount(),
                userCount(), userCount(), similarityVectorPath);
        Path output = DataSetConfig.getV2UserAllocate(threshold);
        runJob(allocateMatrixJob, similarityVectorPath, output, true);
    }
    
    private void multiplyAllocateMatrix() throws Exception {
        Path input = new Path(DataSetConfig.getV2UserAllocate(threshold), "rowVector");
        Path multiplyerPath = new Path(DataSetConfig.getV2UserAllocate(threshold), "columnVector");
        MultiplyMatrixJob matrixAverageJob = new MultiplyMatrixJob(userCount(), userCount(),
                userCount(), multiplyerPath);
        runJob(matrixAverageJob, input, DataSetConfig.getV2UserMultiplyAllocate(threshold)
                , true);
    }
    
    private void doAllocate() throws Exception {
        Path input = new Path(DataSetConfig.getUserSimilarityThresholdPath(threshold), "rowVector");
        Path multiplyer = new Path(DataSetConfig.getV2UserMultiplyAllocate(threshold),
                "columnVector");
        Path output = DataSetConfig.getV2UserDoAllocate(threshold);
        DoAllocateJob doAllocateJob = new DoAllocateJob(userCount(),
                userCount(), userCount(), multiplyer);
        runJob(doAllocateJob, input, output, true);
    }
    
    private void calculateUUThreshold() throws Exception {
        Path doAllocatePath = new Path(DataSetConfig.getV2UserDoAllocate(threshold)
                , "rowVector");
        MultiplyThresholdMatrixJob multiplyThresholdMatrixJob =
                new MultiplyThresholdMatrixJob(userCount(), itemCount(), userCount(),
                        DataSetConfig.getUserItemVectorPath(),
                        threshold, doAllocatePath);
        runJob(multiplyThresholdMatrixJob, DataSetConfig.getUserItemVectorPath(),
                DataSetConfig.getV2UUThresholdPath(threshold), true);
    }
    
    private void calculateUIThreshold() throws Exception {
        int type = MultiplyNearestNeighborJob.TYPE_FIRST;
        Path multipyerPath = DataSetConfig.getItemUserVectorPath();
        Path input = new Path(DataSetConfig.getV2UUThresholdPath(threshold), "rowVector");
        MultiplyNearestNeighborJob multiplyNearestNeighborJob = new MultiplyNearestNeighborJob(userCount(),
                userCount(), itemCount(), multipyerPath, type, k);
        runJob(multiplyNearestNeighborJob, input,
                DataSetConfig.getV2UUUIThresholdPath(threshold), true);
    }
}
