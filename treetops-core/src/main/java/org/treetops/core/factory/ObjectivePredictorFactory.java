package org.treetops.core.factory;

import java.util.HashMap;
import java.util.Map;

import org.treetops.core.model.TreeModel;
import org.treetops.core.predictor.Predictor;
import org.treetops.core.predictor.decorator.BinaryObjectivePredictorDecorator;
import org.treetops.core.predictor.decorator.OutputConvertor;

public class ObjectivePredictorFactory {

    private static final Map<String, Class<? extends OutputConvertor>> registerConvertors = new HashMap<>();

    static {
        registerConvertors.put("binary", BinaryObjectivePredictorDecorator.class);
    }

    public static Predictor decoratePredictorByObjectiveType(Predictor predictor, TreeModel treeModel) {
        try {
            OutputConvertor convertor = registerConvertors.get(treeModel.getObjectiveType()).newInstance();
            return convertor.decorate(predictor, treeModel);
        } catch (Throwable e) {
            // TODO opt exception
            e.printStackTrace();
        }
        return null;
    }
}
