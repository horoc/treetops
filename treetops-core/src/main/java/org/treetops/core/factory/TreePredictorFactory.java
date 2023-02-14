package org.treetops.core.factory;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import org.treetops.core.generator.AsmGenerator;
import org.treetops.core.loader.FileTreeModelLoader;
import org.treetops.core.loader.TreeModelLoader;
import org.treetops.core.model.TreeModel;
import org.treetops.core.predictor.MetaDataHolder;
import org.treetops.core.predictor.Predictor;
import org.treetops.core.predictor.SimplePredictor;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class TreePredictorFactory {

    private static final Map<String, WeakReference<Predictor>> predictors = new HashMap<>();

    private static TreeModelLoader treeModelLoader = FileTreeModelLoader.getInstance();

    private static int ASM_GENERATION_TREE_NUMS_THRESHOLD = 300;

    private TreePredictorFactory() {
    }

    public static synchronized void setTreeModelLoader(TreeModelLoader loader) {
        treeModelLoader = loader;
    }

    public static synchronized void setGenerationTreeNumsThreshold(int threshold) {
        ASM_GENERATION_TREE_NUMS_THRESHOLD = threshold;
    }

    public static synchronized Predictor newInstance(String modelName, String resource) {
        return newInstance(modelName, resource, null);
    }

    public static synchronized Predictor newInstance(String modelName, String resource, boolean enableGeneration) {
        return newInstance(modelName, resource, null, enableGeneration);
    }

    public static synchronized Predictor newInstance(String modelName, String resource, String saveClassFileDir) {
        return newInstance(modelName, resource, saveClassFileDir, true);
    }

    public static synchronized Predictor newInstance(String modelName, String resource, String saveClassFileDir,
                                                     boolean enableGeneration) {
        String className = toClassName(modelName);
        // no longer used predictor will be removed after gc
        if (predictors.containsKey(className)) {
            Predictor predictor = predictors.get(className).get();
            if (predictor != null) {
                return predictor;
            } else {
                predictors.remove(className);
            }
        }

        try {
            TreeModel treeModel = treeModelLoader.loadModel(resource);
            Predictor predictor;
            // if model is too large, downgrade to simple predictor implementation
            if (!enableGeneration || treeModel.getTrees().size() > ASM_GENERATION_TREE_NUMS_THRESHOLD) {
                predictor = new SimplePredictor(treeModel);
            } else {
                // new class loader to do class generation
                AsmGenerator generator = AsmGenerator.getInstance();
                byte[] bytes = generator.generateCode(className, treeModel);
                if (StringUtils.isNotBlank(saveClassFileDir)) {
                    saveClass(bytes, className, saveClassFileDir);
                }
                Object targetObj = generator.defineClassFromCode(className, bytes).newInstance();
                // init meta data if need
                if (targetObj instanceof MetaDataHolder) {
                    ((MetaDataHolder) targetObj).initialize(treeModel);
                }
                predictor = (Predictor) targetObj;
            }

            // objective decorate
            Predictor objectivePredictor = ObjectivePredictorFactory.decoratePredictorByObjectiveType(predictor, treeModel);

            predictors.put(className, new WeakReference<>(objectivePredictor));
            return objectivePredictor;
        } catch (Throwable e) {
            // TODO opt exception
            e.printStackTrace();
        }
        return null;
    }

    private static void saveClass(byte[] bytes, String className, String dir) {
        try {
            String fileName = dir + File.separator + StringUtils.join(className.split("\\."), File.separator) + ".class";
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            file.createNewFile();

            FileOutputStream fos = new FileOutputStream(fileName);
            fos.write(bytes);
            fos.close();
        } catch (Throwable e) {
            // TODO opt exception
            e.printStackTrace();
        }
    }

    private static String toClassName(String modelName) {
        return Predictor.predictorClassPrefix + "._" + modelName;
    }
}
