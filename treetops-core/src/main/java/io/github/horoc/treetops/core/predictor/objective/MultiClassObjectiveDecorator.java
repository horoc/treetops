package io.github.horoc.treetops.core.predictor.objective;

import io.github.horoc.treetops.core.model.TreeModel;
import io.github.horoc.treetops.core.predictor.Predictor;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/18
 */
public class MultiClassObjectiveDecorator extends AbstractOutputConvertor {

    @Override
    public double[] convert(double[] input) {
        double max = input[0];
        double[] output = new double[input.length];
        for (int i = 1; i < input.length; i++) {
            max = max < input[i] ? input[i] : max;
        }

        double expSum = 0.0;
        for (int i = 0; i < input.length; i++) {
            output[i] = Math.exp(input[i] - max);
            expSum += output[i];
        }
        if (expSum == 0.0) {
            return output;
        }
        for (int i = 0; i < output.length; i++) {
            output[i] = output[i] / expSum;
        }
        return output;
    }

    @Override
    public Predictor decorate(Predictor predictor, TreeModel treeModel) {
        this.predictor = predictor;
        return this;
    }
}
