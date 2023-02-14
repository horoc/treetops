package org.treetops.core.predictor.decorator;

import org.treetops.core.model.TreeModel;
import org.treetops.core.predictor.Predictor;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public interface OutputConvertor {

    double[] convert(double[] input);

    Predictor decorate(Predictor predictor, TreeModel treeModel);
}
