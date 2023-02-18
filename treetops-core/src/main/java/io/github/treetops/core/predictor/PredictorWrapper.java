package io.github.treetops.core.predictor;

import io.github.treetops.core.model.TreeModel;
import java.util.Objects;

/**
 * Predictor Wrapper, introduce preprocess/postprocess of prediction process.
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public final class PredictorWrapper implements Predictor {

    private Predictor innerPredictor;

    /**
     * index of feature must not greater than it.
     */
    private int maxFeatureIdx;

    public PredictorWrapper(Predictor innerPredictor, TreeModel treeModel) {
        if (Objects.isNull(innerPredictor)) {
            throw new IllegalArgumentException("new PredictorWrapper error, innerPredictor can not be null");
        }
        this.innerPredictor = innerPredictor;
        this.maxFeatureIdx = treeModel.getMaxFeatureIndex();
    }

    @Override
    public double[] predictRaw(double[] features) {
        checkInputFeature(features);
        return innerPredictor.predictRaw(features);
    }

    @Override
    public double[] predict(double[] features) {
        checkInputFeature(features);
        return innerPredictor.predict(features);
    }

    /**
     * pre-check of input features.
     * @param features input features
     */
    private void checkInputFeature(double[] features) {
        if (Objects.isNull(features) || features.length > maxFeatureIdx + 1) {
            throw new IllegalArgumentException("input features size does not match the predict model");
        }
    }

    /**
     * help gc, clean reference.
     */
    public void release() {
        this.innerPredictor = null;
    }
}
