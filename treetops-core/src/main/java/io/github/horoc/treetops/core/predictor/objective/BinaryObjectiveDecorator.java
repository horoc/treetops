package io.github.horoc.treetops.core.predictor.objective;

import io.github.horoc.treetops.core.model.TreeModel;
import io.github.horoc.treetops.core.predictor.Predictor;
import org.apache.commons.lang3.StringUtils;

/**
 * Binary objective function convertor.
 *
 * @author chenzhou@apache.org
 * created on 2023/2/14
 */
public final class BinaryObjectiveDecorator extends AbstractOutputConvertor {

    private static final String CONFIG_SEPARATOR = ":";

    private double sigmoid;

    @Override
    public Predictor decorate(Predictor predictor, TreeModel treeModel) {
        this.predictor = predictor;
        this.sigmoid = parseSigmoidValue(treeModel.getObjectiveConfig());
        return this;
    }

    @Override
    public double[] convert(double[] input) {
        return new double[]{1.0f / (1.0f + Math.exp(-sigmoid * input[0]))};
    }

    private double parseSigmoidValue(String objectiveConfig) {
        if (StringUtils.isNotBlank(objectiveConfig)) {
            String[] sp = objectiveConfig.split(CONFIG_SEPARATOR);
            if (sp.length >= 1) {
                return Double.parseDouble(sp[1]);
            }
        }
        return 0.0;
    }
}
