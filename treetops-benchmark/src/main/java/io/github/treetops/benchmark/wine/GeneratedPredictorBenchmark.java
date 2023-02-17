package io.github.treetops.benchmark.wine;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.github.treetops.benchmark.common.ClassPathLoader;
import io.github.treetops.core.factory.TreePredictorFactory;
import io.github.treetops.core.predictor.Predictor;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/17
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Group)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)
public class GeneratedPredictorBenchmark {

    private Predictor predictor;

    private double[] features;

    private static final int batchSize = 100;

    @Setup
    public void initPredictor() {
        TreePredictorFactory.setTreeModelLoader(new ClassPathLoader());
        this.predictor = TreePredictorFactory.newInstance("breast_cancer_model_v0",
            "/breast_cancer_model.txt");
        features = new double[30];
        Random r = new Random();
        for (int i = 0; i < features.length; i++) {
            features[i] = -2.0 + 4 * r.nextDouble();
        }
    }

    @Benchmark
    @Group
    public void predict() {
        for (int i = 0; i < batchSize; i++) {
            predictor.predict(features);
        }
    }
}
