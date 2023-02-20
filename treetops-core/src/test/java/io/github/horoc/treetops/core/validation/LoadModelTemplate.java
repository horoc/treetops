package io.github.horoc.treetops.core.validation;

import io.github.horoc.treetops.core.factory.TreePredictorFactory;
import io.github.horoc.treetops.core.predictor.Predictor;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/18
 */
public abstract class LoadModelTemplate {
    /**
     * Get test feature.
     *
     * @return features
     */
    protected abstract double[] getFeature();

    protected Predictor loadModel(String resource, boolean isGenerated) {
        ClassLoader classLoader = getClass().getClassLoader();
        String path = classLoader.getResource(resource + ".txt").getPath();
        return TreePredictorFactory.newInstance(resource, path, isGenerated);
    }
}
