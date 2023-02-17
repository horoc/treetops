package io.github.treetops.core.factory;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import io.github.treetops.core.generator.PredictorClassGenerator;
import io.github.treetops.core.loader.AbstractLoader;
import io.github.treetops.core.loader.FileTreeModelLoader;
import io.github.treetops.core.model.TreeModel;
import io.github.treetops.core.predictor.MetaDataHolder;
import io.github.treetops.core.predictor.Predictor;
import io.github.treetops.core.predictor.SimplePredictor;

/**
 * Core factory of tree predictor, user should only get predictor instance through this factory
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class TreePredictorFactory {

    /**
     * Predictor instance cache pool
     */
    private static final Map<String, WeakReference<Predictor>> PREDICTORS = new HashMap<>();

    private static final Pattern MODEL_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    /**
     * Tree model loader
     */
    private static AbstractLoader treeModelLoader = FileTreeModelLoader.getInstance();

    /**
     * Due to the limitation of asm framework, generated class can not be too large,
     * thus when the tree nums of model larger than threshold,
     * will use {@link SimplePredictor} as predictor implementation.
     * <p>
     * Default threshold is 300, which can meet the requirement of most business scenarios.
     */
    private static int ASM_GENERATION_TREE_NUMS_THRESHOLD = 300;

    /**
     * Entry point for user custom model loader, default model loader is {@link FileTreeModelLoader} <br/>
     * User custom model loader should implement {@link AbstractLoader}
     *
     * @param loader custom loader
     */
    public static synchronized void setTreeModelLoader(AbstractLoader loader) {
        treeModelLoader = loader;
    }

    /**
     * Entry point for user to custom tree nums threshold. <br/>
     * To be careful, the value should not be too large, since asm has a limitation of class size.
     * Can see the detail {@link org.objectweb.asm.ClassWriter#toByteArray()}
     *
     * @param threshold custom value
     */
    public static synchronized void setGenerationTreeNumsThreshold(int threshold) {
        ASM_GENERATION_TREE_NUMS_THRESHOLD = threshold;
    }

    /**
     * Refer to {@link TreePredictorFactory#newInstance(java.lang.String, java.lang.String, java.lang.String, boolean)}
     *
     * @param modelName model name, should be distinct from exist Predictor, and must only contain character: [a-zA-z0-9_]
     * @param resource  resource path, if using default model loader, it means file path
     * @return Predictor instance
     */
    public static synchronized Predictor newInstance(String modelName, String resource) {
        return newInstance(modelName, resource, null);
    }

    /**
     * Refer to {@link TreePredictorFactory#newInstance(java.lang.String, java.lang.String, java.lang.String, boolean)}
     *
     * @param modelName        model name, should be distinct from exist Predictor, and must only contain character: [a-zA-z0-9_]
     * @param resource         resource path, if using default model loader, it means file path
     * @param enableGeneration if enableGeneration is false, will always get {@link SimplePredictor} instance
     * @return Predictor instance
     */
    public static synchronized Predictor newInstance(String modelName, String resource, boolean enableGeneration) {
        return newInstance(modelName, resource, null, enableGeneration);
    }

    /**
     * Refer to {@link TreePredictorFactory#newInstance(java.lang.String, java.lang.String, java.lang.String, boolean)}
     *
     * @param modelName        model name, should be distinct from exist Predictor, and must only contain character: [a-zA-z0-9_]
     * @param resource         resource path, if using default model loader, it means file path
     * @param saveClassFileDir generated Predictor class save path, can be null if it's not necessary
     * @return Predictor instance
     */
    public static synchronized Predictor newInstance(String modelName, String resource, String saveClassFileDir) {
        return newInstance(modelName, resource, saveClassFileDir, true);
    }

    /**
     * Create predictor from resource: <br/>
     * 1. load model data from resource path. <br/>
     * 2. parse to {@link TreeModel} instance.  <br/>
     * 3. generate {@link Predictor} based on model detail.
     *
     * @param modelName        model name, should be distinct from exist Predictor, and must only contain character: [a-zA-z0-9_]
     * @param resource         resource path, if using default model loader, it means file path
     * @param saveClassFileDir generated Predictor class save path, can be null if it's not necessary
     * @param enableGeneration if enableGeneration is false, will always get {@link SimplePredictor} instance
     * @return Predictor instance
     */
    public static synchronized Predictor newInstance(String modelName, String resource, String saveClassFileDir,
                                                     boolean enableGeneration) {
        checkModelName(modelName);

        String className = toClassName(modelName);
        // predictor which is no longer used will be removed after gc
        if (PREDICTORS.containsKey(className)) {
            Predictor predictor = PREDICTORS.get(className).get();
            if (predictor != null) {
                return predictor;
            } else {
                PREDICTORS.remove(className);
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
                PredictorClassGenerator generator = PredictorClassGenerator.getInstance();
                byte[] bytes = generator.generateCode(className, treeModel);
                if (StringUtils.isNotBlank(saveClassFileDir)) {
                    saveClass(bytes, className, saveClassFileDir);
                }
                Object targetObj = generator.defineClassFromCode(className, bytes).newInstance();
                predictor = (Predictor) targetObj;
            }

            // init meta data if need
            if (predictor instanceof MetaDataHolder) {
                ((MetaDataHolder) predictor).initialize(treeModel);
            }

            // objective decorate
            Predictor objectivePredictor = ObjectivePredictorFactory.decoratePredictorByObjectiveType(predictor, treeModel);

            PREDICTORS.put(className, new WeakReference<>(objectivePredictor));
            return objectivePredictor;
        } catch (Throwable e) {
            throw new RuntimeException(String.format("fail to generate predict instance, modelName: %s", modelName), e);
        }
    }

    /**
     * Save raw bytecode into file
     */
    private static void saveClass(byte[] bytes, String className, String dir) throws Exception {
        String fileName = customClassFilePath(className, dir);

        File file = new File(fileName);
        file.getParentFile().mkdirs();
        file.createNewFile();

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(bytes);
        }
    }

    private static String customClassFilePath(String className, String dir) {
        return StringUtils.join(dir, File.separator, StringUtils.join(className.split("\\."), File.separator), ".class");
    }

    /**
     * Generated class will start with '_'
     */
    private static String toClassName(String modelName) {
        return Predictor.PREDICTOR_CLASS_PREFIX + "._" + modelName;
    }

    private static void checkModelName(String modelName) {
        if (!MODEL_NAME_PATTERN.matcher(modelName).matches()) {
            throw new IllegalArgumentException(String.format("illegal model name: %s, valid character: [a-zA-z0-9_]", modelName));
        }
    }
}
