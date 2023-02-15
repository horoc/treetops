package org.treetops.core.predictor.decorator;

import org.treetops.core.model.TreeModel;
import org.treetops.core.predictor.Predictor;

/**
 * Objective convert function decorator interface
 * <P></P>
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public interface OutputConvertor {

    /**
     * Convert input based on certain objective type
     *
     * @param input raw input data
     * @return output data
     */
    double[] convert(double[] input);

    /**
     * Binding to a predictor
     *
     * @param predictor predictor which is need to be decorated
     * @param treeModel tree model
     * @return decorated predictor
     */
    Predictor decorate(Predictor predictor, TreeModel treeModel);
}
