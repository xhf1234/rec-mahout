/**
 * 2013-3-16
 * 
 * xuhongfeng
 */
package hongfeng.xu.rec.mahout.hadoop.threshold;

import hongfeng.xu.rec.mahout.config.MovielensDataConfig;
import hongfeng.xu.rec.mahout.hadoop.HadoopHelper;
import hongfeng.xu.rec.mahout.hadoop.recommender.BaseRecommender;
import hongfeng.xu.rec.mahout.hadoop.recommender.PopularityItemJob;
import hongfeng.xu.rec.mahout.hadoop.recommender.RecommendedItem;
import hongfeng.xu.rec.mahout.hadoop.recommender.RecommendedItemList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.math.Vector;
import org.apache.mahout.math.VectorWritable;

/**
 * @author xuhongfeng
 *
 */
public class PopularRecommender extends BaseRecommender {
    
    @Override
    public int run(String[] args) throws Exception {
        addInputOption();
        addOutputOption();
        
        Map<String,List<String>> parsedArgs = parseArguments(args);
        if (parsedArgs == null) {
          return -1;
        }
        AtomicInteger currentPhase = new AtomicInteger();
        
        if(shouldRunNextPhase(parsedArgs, currentPhase)) {
            if (!HadoopHelper.isFileExists(MovielensDataConfig.getPopularItemSortPath(), getConf())) {
                Tool job = new PopularityItemJob();
                ToolRunner.run(job, new String[] {
                        "--input", MovielensDataConfig.getItemUserVectorPath().toString(),
                        "--output", MovielensDataConfig.getPopularItemSortPath().toString()
                });
            }
        }
        
        if(shouldRunNextPhase(parsedArgs, currentPhase)) {
            Job job = prepareJob(getInputPath(), getOutputPath(), SequenceFileInputFormat.class,
                    MyMapper.class, IntWritable.class, RecommendedItemList.class,
                    SequenceFileOutputFormat.class);
            if (!job.waitForCompletion(true)) {
                return -1;
            }
        }
        
        return 0;
    }

    public static class MyMapper extends Mapper<IntWritable, VectorWritable,
        IntWritable, RecommendedItemList> {
        private PopularItemQueue queue;

        public MyMapper() {
            super();
        }
        
        @Override
        protected void setup(Context context)
                throws IOException, InterruptedException {
            super.setup(context);
            queue = PopularItemQueue.create(context.getConfiguration());
        }
        
        @Override
        protected void map(IntWritable key, VectorWritable value,
                Context context)
                throws IOException, InterruptedException {
            int n = MovielensDataConfig.TOP_N;
            List<RecommendedItem> items = new ArrayList<RecommendedItem>();
            Vector vector = value.get();
            int i = 0;
            while (items.size() < n) {
                int itemId = queue.getItemId(i++);
                boolean exists = false;
                for (RecommendedItem item:items) {
                    if (item.getId() == itemId) {
                        exists = true;
                        break;
                    }
                }
                if (exists) {
                    continue;
                }
                double pref = vector.getQuick(itemId);
                if (pref != 0) {
                    continue;
                }
                items.add(new RecommendedItem(itemId, 1.0));
            }
            context.write(key, new RecommendedItemList(items));
        }
    }
}