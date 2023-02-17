package io.github.treetops.benchmark.california_housing;

import java.util.Random;

import io.github.treetops.benchmark.common.AverageTimeBenchmarkTemplate;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/17
 */
public class GeneratedPredictorBenchmark extends AverageTimeBenchmarkTemplate {

    @Override
    protected String modelName() {
        return "california_housing_model";
    }

    @Override
    protected boolean isGenerated() {
        return true;
    }

    @Override
    protected double[] getFeature() {
        features = new double[8];
        Random r = new Random();
        for (int i = 0; i < features.length; i++) {
            features[i] = -2.0 + 4 * r.nextDouble();
        }
        return features;
    }
}
