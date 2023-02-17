package io.github.treetops.benchmark.common;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Group;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;

import io.github.treetops.core.factory.TreePredictorFactory;
import io.github.treetops.core.predictor.Predictor;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/18
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Group)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public abstract class AverageTimeBenchmarkTemplate {

    protected Predictor predictor;

    protected double[] features;

    private static final int batchSize = 100;

    /**
     * model name, model file should be modelName.txt
     *
     * @return
     */
    protected abstract String modelName();

    /**
     * is test predictor enable generated
     *
     * @return
     */
    protected abstract boolean isGenerated();

    /**
     * get test feature
     *
     * @return
     */
    protected abstract double[] getFeature();

    @Setup
    public void setup() {
        TreePredictorFactory.setTreeModelLoader(new ClassPathLoader());
        this.predictor = TreePredictorFactory.newInstance(modelName(), "/" + modelName() + ".txt", isGenerated());
        features = getFeature();
    }

    @Benchmark
    @Group
    public void predict() {
        for (int i = 0; i < batchSize; i++) {
            predictor.predict(features);
        }
    }
}
