package io.github.horoc.treetops.core.predictor.objective;

import io.github.horoc.treetops.core.model.TreeModel;
import io.github.horoc.treetops.core.predictor.Predictor;

/**
 * Objective convert function interface.
 * <br>
 *
 * @author chenzhou@apache.org
 * created on 2023/2/14
 */
public abstract class AbstractOutputConvertor implements Predictor {

    protected Predictor predictor;

    /**
     * Convert input based on certain objective strategy.
     *
     * @param input raw input data
     * @return output data
     */
    public abstract double[] convert(double[] input);

    /**
     * Binding to a predictor.
     *
     * @param predictor predictor which is need to be decorated
     * @param treeModel tree model
     * @return decorated predictor
     */
    public abstract Predictor decorate(Predictor predictor, TreeModel treeModel);

    @Override
    public double[] predictRaw(double[] features) {
        return predictor.predictRaw(features);
    }

    @Override
    public double[] predict(double[] features) {
        return convert(predictor.predictRaw(features));
    }
}
