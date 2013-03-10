/**
 * 2013-3-10
 * 
 * xuhongfeng
 */
package hongfeng.xu.rec.mahout.hadoop.parser;

import hongfeng.xu.rec.mahout.model.DeliciousDataModel.RawDataLine;

import java.io.IOException;
import java.util.Random;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

/**
 * @author xuhongfeng
 *
 */
public final class ParserMapper extends Mapper<LongWritable, Text, KeyType, DoubleWritable> {
    private static final double TRAINING_RATE = 0.9;
    private final Random random = new Random();
    
    public ParserMapper() {
        super();
    }



    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        RawDataLine line = RawDataLine.parse(value.toString());
        if (random.nextDouble() <= TRAINING_RATE) {
            context.write(new KeyType(KeyType.TYPE_USER_ITEM, line.userId, line.bookmarkId), new DoubleWritable(1.0));
            context.write(new KeyType(KeyType.TYPE_USER_TAG, line.userId, line.tagId), new DoubleWritable(1.0));
            context.write(new KeyType(KeyType.TYPE_ITEM_TAG, line.bookmarkId, line.tagId), new DoubleWritable(1.0));
        } else {
            context.write(new KeyType(KeyType.TYPE_TEST_DATA, line.userId, line.bookmarkId), new DoubleWritable(1.0));
        }
    }
}