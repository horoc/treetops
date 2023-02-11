package org.treetops.core.predictor.decorator;

import java.util.concurrent.ConcurrentHashMap;

import org.treetops.core.model.TreeModel;
import org.treetops.core.predictor.Predictor;

public interface OutputConvertor {

    double[] convert(double[] input);

    Predictor decorate(Predictor predictor, TreeModel treeModel);
}
