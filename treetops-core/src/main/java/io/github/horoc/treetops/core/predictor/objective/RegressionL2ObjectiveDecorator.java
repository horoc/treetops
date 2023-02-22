package io.github.horoc.treetops.core.predictor.objective;

import io.github.horoc.treetops.core.model.TreeModel;
import io.github.horoc.treetops.core.predictor.Predictor;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/15
 */
public final class RegressionL2ObjectiveDecorator extends AbstractOutputConvertor {

    private static final String SQRT_CONFIG_KEY = "sqrt";

    private boolean sqrt;

    @Override
    public double[] convert(double[] input) {
        double ret = input[0];
        if (sqrt) {
            if (!Double.isNaN(ret)) {
                ret = ret * ret * (ret >= 0 ? 1 : -1);
            }
        }
        return new double[]{ret};
    }

    @Override
    public Predictor decorate(Predictor predictor, TreeModel treeModel) {
        this.predictor = predictor;
        if (SQRT_CONFIG_KEY.equals(treeModel.getObjectiveConfig())) {
            sqrt = true;
        }
        return this;
    }
}
