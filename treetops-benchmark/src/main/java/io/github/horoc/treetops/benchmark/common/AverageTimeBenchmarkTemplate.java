package io.github.horoc.treetops.benchmark.common;

import io.github.horoc.treetops.core.factory.TreePredictorFactory;
import io.github.horoc.treetops.core.predictor.Predictor;
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

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Group)
@Warmup(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(2)
public abstract class AverageTimeBenchmarkTemplate {

    private static final int BATCH_SIZE = 500;

    @SuppressWarnings("checkstyle:VisibilityModifier")
    protected Predictor predictor;

    @SuppressWarnings("checkstyle:VisibilityModifier")
    protected double[] features;

    /**
     * model name, model file should be modelName.txt.
     *
     * @return model name
     */
    protected abstract String modelName();

    /**
     * is test predictor enable generated.
     *
     * @return is generated predictor
     */
    protected abstract boolean isGenerated();

    /**
     * Get test feature.
     *
     * @return features
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
        for (int i = 0; i < BATCH_SIZE; i++) {
            predictor.predict(features);
        }
    }
}
