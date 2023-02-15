package org.treetops.core.predictor.decorator;

import java.math.BigDecimal;

import org.treetops.core.model.TreeModel;
import org.treetops.core.predictor.Predictor;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/15
 */
public class RegressionObjectiveDecorator implements OutputConvertor, Predictor {

    private static final String SQRT_CONFIG_KEY = "sqrt";

    private Predictor predictor;
    private boolean sqrt = false;

    @Override
    public double[] predict(double[] features) {
        return convert(predictor.predict(features));
    }

    @Override
    public double[] predictRaw(double[] features) {
        return predictor.predictRaw(features);
    }

    @Override
    public double[] convert(double[] input) {
        double ret = input[0];
        if (sqrt) {
            if (!Double.isNaN(ret)) {
                ret = ret * ret * (ret >= 0 ? 1 : -1);
            }
        }
        return new double[] {ret};
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
