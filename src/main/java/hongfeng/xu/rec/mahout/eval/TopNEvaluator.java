/**
 * 2013-1-12
 * 
 * xuhongfeng
 */
package hongfeng.xu.rec.mahout.eval;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.Recommender;

/**
 * @author xuhongfeng
 *
 */
public interface TopNEvaluator {
    public double evaluate(Recommender recommender, DataModel totalDataModel 
            , DataModel testDataModel, int N) throws TasteException;
}