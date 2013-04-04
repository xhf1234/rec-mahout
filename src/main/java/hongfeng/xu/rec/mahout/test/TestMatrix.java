/**
 * 2013-3-30
 * 
 * xuhongfeng
 */
package hongfeng.xu.rec.mahout.test;

import hongfeng.xu.rec.mahout.config.DataSetConfig;
import hongfeng.xu.rec.mahout.hadoop.HadoopHelper;
import hongfeng.xu.rec.mahout.hadoop.MultipleSequenceOutputFormat;
import hongfeng.xu.rec.mahout.hadoop.matrix.VectorCache;
import hongfeng.xu.rec.mahout.structure.FixedSizePriorityQueue;
import hongfeng.xu.rec.mahout.util.L;

import java.io.IOException;
import java.util.Comparator;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.util.ToolRunner;
import org.apache.mahout.common.AbstractJob;
import org.apache.mahout.common.HadoopUtil;
import org.apache.mahout.common.Pair;
import org.apache.mahout.common.iterator.sequencefile.PathType;
import org.apache.mahout.common.iterator.sequencefile.SequenceFileDirIterator;
import org.apache.mahout.math.VectorWritable;

/**
 * @author xuhongfeng
 *
 */
public class TestMatrix extends AbstractJob {

    @Override
    public int run(String[] args) throws Exception {
        testUUThreshold();
//        testItemUserVector();
//        testItemOneZeroCount();
        return 0;
    }

    public static void main(String[] args) {
        try {
            ToolRunner.run(new TestMatrix(), new String[] {
                "--input", "test",
                "--output", "test"
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void testUUThreshold() throws IOException {
        int threshold = 10;
//        int vectorCount = HadoopUtil.readInt(DataSetConfig.getUserCountPath(), getConf());
//        int vectorSize = vectorCount;
//        Path path = new Path(DataSetConfig.getUUThresholdPath(threshold), "rowVector");
//        VectorCache cache = VectorCache.create(vectorCount, vectorSize, path, getConf());
//        for (int i=0; i<vectorCount; i++) {
//            HadoopHelper.log(this, "zSum = " + cache.get(i).zSum());
//        }
        
//        Path path = new Path(DataSetConfig.getUUThresholdPath(threshold), "distribution");
//        SequenceFileDirIterator<DoubleWritable, IntWritable> iterator
//            = new SequenceFileDirIterator<DoubleWritable, IntWritable>(path, PathType.LIST,
//                    MultipleSequenceOutputFormat.FILTER, null, true, getConf());
//        while (iterator.hasNext()) {
//            Pair<DoubleWritable, IntWritable> pair = iterator.next();
//            HadoopHelper.log(this, pair.toString());
//        }
//        iterator.close();
        
        Path averageSimilarityPath = new Path(DataSetConfig.getSimilarityThresholdAveragePath(threshold), "rowVector");
        int vectorCount = HadoopUtil.readInt(DataSetConfig.getUserCountPath(), getConf());
        int vectorSize = vectorCount;
        Path path = new Path(DataSetConfig.getUUThresholdPath(30), "rowVector");
        VectorCache cache = VectorCache.create(vectorCount, vectorSize, path, getConf());
        for (int i=0; i<vectorCount; i++) {
            HadoopHelper.log(this, "zSum = " + cache.get(i).zSum());
        }
    }
    
    private void testUserVectorCount() throws IOException {
        Path path = DataSetConfig.getUserItemVectorPath();
        SequenceFileDirIterator<IntWritable, VectorWritable> iterator
            = HadoopHelper.openVectorIterator(path, getConf());
        int count = 0;
        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }
        HadoopHelper.log(this, "count=" + count);
        if (count != 943) {
            assertFailed("count = " + count);
        }
        iterator.close();
    }
    
    private void assertFailed(String msg) {
        throw new RuntimeException(msg);
    }
    
    private void testItemUserVector() throws IOException {
        int itemCount = HadoopUtil.readInt(DataSetConfig.getItemCountPath(), getConf());
        SequenceFileDirIterator<IntWritable, VectorWritable> iterator = HadoopHelper.openVectorIterator(DataSetConfig.getItemUserVectorPath(), getConf());
        int count = 0;
        while (iterator.hasNext()) {
            iterator.next();
            count++;
        }
        if (count != itemCount) {
            assertFailed("count = " + count);
        }
        iterator.close();
    }
    
    private void testItemOneZeroCount() throws IOException {
        int itemCount = HadoopUtil.readInt(DataSetConfig.getItemCountPath(), getConf());
        HadoopHelper.log(this, "itemCount=" + itemCount);
        if (itemCount != 1682) {
            assertFailed("itemCount = " + itemCount);
        }
        
        int count = (itemCount-1)*itemCount/2;
        
        
        SequenceFileDirIterator<IntWritable, IntWritable> iterator = 
                new SequenceFileDirIterator<IntWritable, IntWritable>(DataSetConfig.getCountIUUIOneZeroPath(),
                PathType.LIST, MultipleSequenceOutputFormat.FILTER, null, true,
                getConf());
        int c = 0;
        while (iterator.hasNext()) {
            Pair<IntWritable, IntWritable> pair = iterator.next();
            c += pair.getSecond().get();
        }
        if (c != count) {
            assertFailed("c = " + c);
        }
        iterator.close();
    }
    
    private static void testNearest() {
        int k = 3;
        FixedSizePriorityQueue<Pair<Double, Double>> queue =
                new FixedSizePriorityQueue<Pair<Double,Double>>(k,
            new Comparator<Pair<Double, Double>>() {
                @Override
                public int compare(Pair<Double, Double> o1,
                        Pair<Double, Double> o2) {
                    if (o1.getFirst() > o2.getFirst()) {
                        return 1;
                    }
                    if (o1.getFirst() < o2.getFirst()) {
                        return -1;
                    }
                    return 0;
                }
        });
        queue.add(new Pair<Double, Double>(1.0, 1.0));
        queue.add(new Pair<Double, Double>(3.0, 1.0));
        queue.add(new Pair<Double, Double>(5.0, 1.0));
        queue.add(new Pair<Double, Double>(2.0, 1.0));
        queue.add(new Pair<Double, Double>(4.0, 1.0));
        for (Pair<Double, Double> pair:queue) {
            L.i("", "" + pair.getFirst());
        }
    }
}
