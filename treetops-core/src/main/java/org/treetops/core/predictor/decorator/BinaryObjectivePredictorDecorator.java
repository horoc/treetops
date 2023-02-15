package org.treetops.core.predictor.decorator;

import org.apache.commons.lang3.StringUtils;

import org.treetops.core.model.TreeModel;
import org.treetops.core.predictor.Predictor;

/**
 * Binary objective function convertor
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class BinaryObjectivePredictorDecorator implements OutputConvertor, Predictor {

    private Predictor predictor;
    private double sigmoid;

    @Override
    public Predictor decorate(Predictor predictor, TreeModel treeModel) {
        this.predictor = predictor;
        this.sigmoid = parseSigmoidValue(treeModel.getObjectiveConfig());
        return this;
    }

    @Override
    public double[] predict(double[] features) {
        return predictor.predict(features);
    }

    @Override
    public double[] predictRaw(double[] features) {
        return convert(predictor.predict(features));
    }

    @Override
    public double[] convert(double[] input) {
        return new double[] {1.0f / (1.0f + Math.exp(-sigmoid * input[0]))};
    }

    private double parseSigmoidValue(String objectiveConfig) {
        if (StringUtils.isNotBlank(objectiveConfig)) {
            String[] sp = objectiveConfig.split(":");
            if (sp.length >= 1) {
                return Double.parseDouble(sp[1]);
            }
        }
        return 0.0;
    }
}
