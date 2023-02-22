package io.github.horoc.treetops.core.predictor;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/14
 */
public interface Predictor {

    /**
     * All implementation of Predictor should be subclass of this package.
     */
    String PREDICTOR_CLASS_PREFIX = "io.github.treetops.core.predictor";

    /**
     * Refer to official library api: microsoft/LightGBM/src/boosting/gbdt.h#GBDT::PredictRaw.
     * <br>
     *
     * @param features input feature, size of features should be equals to max_feature_idx
     * @return output value, size of output should be num_class
     */
    double[] predictRaw(double[] features);

    /**
     * Refer to official library api: microsoft/LightGBM/src/boosting/gbdt.h#GBDT::Predict.
     * <br>
     *
     * @param features input feature, size of features should be equals to max_feature_idx
     * @return output value, size of output should be num_class
     */
    default double[] predict(double[] features) {
        return predictRaw(features);
    }
}

