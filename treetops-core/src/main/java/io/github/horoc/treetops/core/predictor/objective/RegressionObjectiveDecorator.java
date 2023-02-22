package io.github.horoc.treetops.core.predictor.objective;

import io.github.horoc.treetops.core.model.TreeModel;
import io.github.horoc.treetops.core.predictor.Predictor;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/19
 */
public class RegressionObjectiveDecorator extends AbstractOutputConvertor {

    @Override
    public double[] convert(double[] input) {
        return new double[] {Math.exp(input[0])};
    }

    @Override
    public Predictor decorate(Predictor predictor, TreeModel treeModel) {
        this.predictor = predictor;
        return this;
    }
}
