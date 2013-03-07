/**
 * 2013-3-10
 * 
 * xuhongfeng
 */
package hongfeng.xu.rec.mahout.hadoop.parser;

import hongfeng.xu.rec.mahout.hadoop.HadoopHelper;
import hongfeng.xu.rec.mahout.util.L;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.common.AbstractJob;

/**
 * @author xuhongfeng
 *
 */
public class RawDataParser extends AbstractJob {
    public static final String FILE_USER_ITEM = "userItem.data";
    public static final String FILE_USER_TAG = "userTag.data";
    public static final String FILE_ITEM_TAG = "itemTag.data";
    
    @Override
    public int run(String[] args) throws Exception {
        addInputOption();
        addOutputOption();

        Map<String,List<String>> parsedArgs = parseArguments(args);
        if (parsedArgs == null) {
          return -1;
        }
        
        if (alreadyDone()) {
            return 0;
        }
        
        Job job = prepareJob(getInputPath(), getOutputPath(), TextInputFormat.class, 
                ParserMapper.class, KeyType.class, DoubleWritable.class, ParserReducer.class,
                KeyType.class, DoubleWritable.class, ParserOutputFormat.class);
        job.setCombinerClass(ParserReducer.class);
        if (!job.waitForCompletion(true)) {
            return -1;
        }
        
        return 0;
    }
    
    private boolean alreadyDone() throws IOException {
        return HadoopHelper.isFileExists(new Path(getOutputPath(), FILE_USER_ITEM), getConf())
            && HadoopHelper.isFileExists(new Path(getOutputPath(), FILE_USER_TAG), getConf())
            && HadoopHelper.isFileExists(new Path(getOutputPath(), FILE_ITEM_TAG), getConf());
    }
    
    public static void main(String args[]) {
        RawDataParser job = new RawDataParser();
        try {
            ToolRunner.run(job, args);
        } catch (Exception e) {
            L.e(job, e);
        }
    }
    
} 