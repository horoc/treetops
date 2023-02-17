package io.github.treetops.core.factory;

import java.util.HashMap;
import java.util.Map;

import io.github.treetops.core.model.TreeModel;
import io.github.treetops.core.predictor.Predictor;
import io.github.treetops.core.predictor.decorator.BinaryObjectiveDecorator;
import io.github.treetops.core.predictor.decorator.AbstractOutputConvertor;
import io.github.treetops.core.predictor.decorator.RegressionObjectiveDecorator;

/**
 * Predictor objective function decorate factory
 * <p></p>
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class ObjectivePredictorFactory {

    private static final Map<String, Class<? extends AbstractOutputConvertor>> CONVERTORS = new HashMap<>();

    /**
     * Any new objective convertor should register into the container first
     */
    static {
        CONVERTORS.put("binary", BinaryObjectiveDecorator.class);
        CONVERTORS.put("regression", RegressionObjectiveDecorator.class);
        CONVERTORS.put("regression_l1", RegressionObjectiveDecorator.class);
    }

    /**
     * Get decorated predictor instance based on tree model config
     *
     * @param predictor raw predictor
     * @param treeModel tree model
     * @return decorated predictor
     * @throws Exception throw exception when getting convertor instance failed
     */
    static Predictor decoratePredictorByObjectiveType(Predictor predictor, TreeModel treeModel) throws Exception {
        Class<? extends AbstractOutputConvertor> convertorClass = CONVERTORS.get(treeModel.getObjectiveType());
        if (convertorClass == null) {
            throw new RuntimeException(String.format("unsupported objective type : %s", treeModel.getObjectiveType()));
        }

        AbstractOutputConvertor convertor = convertorClass.newInstance();
        return convertor.decorate(predictor, treeModel);
    }

    /**
     * Entry point for custom convertor,
     * new convertor class should implement {@link AbstractOutputConvertor}
     *
     * @param type  type name, should be distinct from exist convertor
     * @param clazz class
     */
    public static void registerNewConvertor(String type, Class<? extends AbstractOutputConvertor> clazz) {
        CONVERTORS.putIfAbsent(type, clazz);
    }
}
