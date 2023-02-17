package io.github.treetops.core.predictor.decorator;

import java.util.Arrays;

import io.github.treetops.core.model.TreeModel;
import io.github.treetops.core.predictor.Predictor;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/18
 */
public class MultiClassObjectiveDecorator extends AbstractOutputConvertor {

    @Override
    public double[] convert(double[] input) {
        double expSum = 0.0;
        for (int i = 0; i < input.length; i++) {
            expSum += Math.exp(input[i]);
        }
        if (expSum == 0.0) {
            return Arrays.copyOf(input, input.length);
        }

        double[] output = new double[input.length];
        for (int i = 0; i < output.length; i++) {
            output[i] = input[i] / expSum;
        }
        return output;
    }

    @Override
    public Predictor decorate(Predictor predictor, TreeModel treeModel) {
        return this.predictor = predictor;
    }
}
