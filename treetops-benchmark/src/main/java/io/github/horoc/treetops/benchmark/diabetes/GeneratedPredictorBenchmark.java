package io.github.horoc.treetops.benchmark.diabetes;

import io.github.horoc.treetops.benchmark.common.AverageTimeBenchmarkTemplate;
import java.util.Random;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/17
 */
public class GeneratedPredictorBenchmark extends AverageTimeBenchmarkTemplate {

    @Override
    protected String modelName() {
        return "diabetes_model";
    }

    @Override
    protected boolean isGenerated() {
        return true;
    }

    @Override
    protected double[] getFeature() {
        features = new double[10];
        Random r = new Random();
        for (int i = 0; i < features.length; i++) {
            if (i == 1) {
                features[i] = r.nextInt(2);
            } else {
                features[i] = -2.0 + 4 * r.nextDouble();
            }
        }
        return features;
    }
}
