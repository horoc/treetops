package io.github.treetops.core.predictor;

import java.util.Objects;

public final class PredictorWrapper implements Predictor {

    private Predictor innerPredictor;

    public PredictorWrapper(Predictor innerPredictor) {
        if (Objects.isNull(innerPredictor)) {
            throw new IllegalArgumentException("new PredictorWrapper error, innerPredictor can not be null");
        }
    }

    @Override
    public double[] predictRaw(double[] features) {
        return innerPredictor.predictRaw(features);
    }

    @Override
    public double[] predict(double[] features) {
        return innerPredictor.predict(features);
    }

    /**
     * help gc
     */
    public void release() {
        this.innerPredictor = null;
    }
}
