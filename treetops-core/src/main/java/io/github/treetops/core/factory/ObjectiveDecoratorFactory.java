package io.github.treetops.core.factory;

import io.github.treetops.core.model.TreeModel;
import io.github.treetops.core.predictor.Predictor;
import io.github.treetops.core.predictor.objective.AbstractOutputConvertor;
import io.github.treetops.core.predictor.objective.BinaryObjectiveDecorator;
import io.github.treetops.core.predictor.objective.CrossEntropyLambdaObjectiveDecorator;
import io.github.treetops.core.predictor.objective.CrossEntropyObjectiveConvertor;
import io.github.treetops.core.predictor.objective.MultiClassObjectiveDecorator;
import io.github.treetops.core.predictor.objective.RegressionL2ObjectiveDecorator;
import io.github.treetops.core.predictor.objective.RegressionObjectiveDecorator;
import java.util.HashMap;
import java.util.Map;

/**
 * Predictor objective function decorate factory.
 * <p></p>
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class ObjectiveDecoratorFactory {

    private static final Map<String, Class<? extends AbstractOutputConvertor>> CONVERTORS = new HashMap<>();

    /**
     * Any new objective convertor should register into the container first
     */
    static {
        CONVERTORS.put("binary", BinaryObjectiveDecorator.class);
        CONVERTORS.put("regression", RegressionL2ObjectiveDecorator.class);
        CONVERTORS.put("regression_l1", RegressionL2ObjectiveDecorator.class);
        CONVERTORS.put("quantile", RegressionL2ObjectiveDecorator.class);
        CONVERTORS.put("huber", RegressionL2ObjectiveDecorator.class);
        CONVERTORS.put("fair", RegressionL2ObjectiveDecorator.class);
        CONVERTORS.put("mape", RegressionL2ObjectiveDecorator.class);
        CONVERTORS.put("poisson", RegressionObjectiveDecorator.class);
        CONVERTORS.put("gamma", RegressionObjectiveDecorator.class);
        CONVERTORS.put("tweedie", RegressionObjectiveDecorator.class);
        CONVERTORS.put("multiclass", MultiClassObjectiveDecorator.class);
        CONVERTORS.put("cross_entropy", CrossEntropyObjectiveConvertor.class);
        CONVERTORS.put("cross_entropy_lambda", CrossEntropyLambdaObjectiveDecorator.class);
    }

    /**
     * Get decorated predictor instance based on tree model config.
     *
     * @param predictor raw predictor
     * @param treeModel tree model
     * @return decorated predictor
     * @throws Exception throw exception when getting convertor instance failed
     */
    static Predictor decoratePredictorByObjectiveType(final Predictor predictor, final TreeModel treeModel) throws Exception {
        Class<? extends AbstractOutputConvertor> convertorClass = CONVERTORS.get(treeModel.getObjectiveType());
        if (convertorClass == null) {
            throw new RuntimeException(String.format("unsupported objective type : %s", treeModel.getObjectiveType()));
        }

        AbstractOutputConvertor convertor = convertorClass.newInstance();
        return convertor.decorate(predictor, treeModel);
    }

    /**
     * Entry point for custom convertor,
     * new convertor class should implement {@link AbstractOutputConvertor}.
     *
     * @param type  type name, should be distinct from exist convertor
     * @param clazz class
     */
    public static void registerNewConvertor(final String type, final Class<? extends AbstractOutputConvertor> clazz) {
        CONVERTORS.putIfAbsent(type, clazz);
    }

    /**
     * Check if objective type is supported.
     *
     * @param type type name
     * @return is supported
     */
    public static boolean isValidObjectiveType(final String type) {
        return CONVERTORS.containsKey(type);
    }
}
