package io.github.horoc.treetops.core.factory;

import io.github.horoc.treetops.core.generator.PredictorClassGenerator;
import io.github.horoc.treetops.core.loader.AbstractLoader;
import io.github.horoc.treetops.core.loader.FileTreeModelLoader;
import io.github.horoc.treetops.core.model.TreeModel;
import io.github.horoc.treetops.core.predictor.MetaDataHolder;
import io.github.horoc.treetops.core.predictor.Predictor;
import io.github.horoc.treetops.core.predictor.PredictorWrapper;
import io.github.horoc.treetops.core.predictor.SimplePredictor;
import java.io.File;
import java.io.FileOutputStream;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.annotation.concurrent.ThreadSafe;
import org.apache.commons.lang3.StringUtils;

/**
 * Core factory of tree predictor, user should only get predictor instance through this factory.
 *
 * @author chenzhou@apache.org
 * created on 2023/2/14
 */
@ThreadSafe
@ParametersAreNonnullByDefault
public class TreePredictorFactory {

    /**
     * Predictor instance cache pool.
     */
    private static final Map<String, WeakReference<Predictor>> PREDICTORS = new HashMap<>();

    private static final Pattern MODEL_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    /**
     * Tree model loader.
     */
    private static AbstractLoader treeModelLoader = FileTreeModelLoader.getInstance();

    /**
     * Due to the limitation of asm framework, generated class can not be too large,
     * thus when the tree nums of model larger than threshold,
     * will use {@link SimplePredictor} as predictor implementation.
     * <p>
     * Default threshold is 300, which can meet the requirement of most business scenarios.
     */
    private static int asmGenerationTreeNumsThreshold = 300;

    /**
     * Entry point for user custom model loader, default model loader is {@link FileTreeModelLoader} <br>
     * User custom model loader should implement {@link AbstractLoader}.
     *
     * @param loader custom loader
     */
    public static synchronized void setTreeModelLoader(final AbstractLoader loader) {
        treeModelLoader = loader;
    }

    /**
     * Entry point for user to custom tree nums threshold. <br>
     * To be careful, the value should not be too large, since asm has a limitation of class size.
     * Can see the detail {@link org.objectweb.asm.ClassWriter#toByteArray()}.
     *
     * @param threshold custom value
     */
    public static synchronized void setGenerationTreeNumsThreshold(final int threshold) {
        asmGenerationTreeNumsThreshold = threshold;
    }

    /**
     * Refer to {@link TreePredictorFactory#newInstance(java.lang.String, java.lang.String, java.lang.String, boolean)}.
     *
     * @param modelName model name, should be distinct from exist Predictor, and must only contain character: [a-zA-z0-9_]
     * @param resource  resource path, if using default model loader, it means file path
     * @return Predictor instance
     */
    public static synchronized Predictor newInstance(final String modelName, final String resource) {
        return newInstance(modelName, resource, null);
    }

    /**
     * Refer to {@link TreePredictorFactory#newInstance(java.lang.String, java.lang.String, java.lang.String, boolean)}.
     *
     * @param modelName        model name, should be distinct from exist Predictor, and must only contain character: [a-zA-z0-9_]
     * @param resource         resource path, if using default model loader, it means file path
     * @param enableGeneration if enableGeneration is false, will always get {@link SimplePredictor} instance
     * @return Predictor instance
     */
    public static synchronized Predictor newInstance(final String modelName, final String resource, boolean enableGeneration) {
        return newInstance(modelName, resource, null, enableGeneration);
    }

    /**
     * Refer to {@link TreePredictorFactory#newInstance(java.lang.String, java.lang.String, java.lang.String, boolean)}.
     *
     * @param modelName        model name, should be distinct from exist Predictor, and must only contain character: [a-zA-z0-9_]
     * @param resource         resource path, if using default model loader, it means file path
     * @param saveClassFileDir generated Predictor class save path, can be null if it's not necessary
     * @return Predictor instance
     */
    public static synchronized Predictor newInstance(final String modelName, final String resource, final String saveClassFileDir) {
        return newInstance(modelName, resource, saveClassFileDir, true);
    }

    /**
     * Create predictor from resource: <br>
     * 1. load model data from resource path. <br>
     * 2. parse to {@link TreeModel} instance.  <br>
     * 3. generate {@link Predictor} based on model detail.
     *
     * @param modelName        model name, should be distinct from exist Predictor, and must only contain character: [a-zA-z0-9_]
     * @param resource         resource path, if using default model loader, it means file path
     * @param saveClassFileDir generated Predictor class save path, can be null if it's not necessary
     * @param enableGeneration if enableGeneration is false, will always get {@link SimplePredictor} instance
     * @return Predictor instance
     */
    public static synchronized Predictor newInstance(final String modelName, final String resource, final String saveClassFileDir,
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
            if (!enableGeneration || treeModel.getTrees().size() > asmGenerationTreeNumsThreshold) {
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
            Predictor objectivePredictor = ObjectiveDecoratorFactory.decoratePredictorByObjectiveType(predictor, treeModel);

            // wrapper
            Predictor predictorWrapper = new PredictorWrapper(objectivePredictor, treeModel);

            PREDICTORS.put(className, new WeakReference<>(predictorWrapper));
            return predictorWrapper;
        } catch (Throwable e) {
            throw new RuntimeException(String.format("fail to generate predict instance, modelName: %s", modelName), e);
        }
    }

    /**
     * Clean predictor reference.
     *
     * @param predictor should be PredictorWrapper instance which created by factory
     */
    private static synchronized void releasePredictor(Predictor predictor) {
        if (predictor instanceof PredictorWrapper) {
            ((PredictorWrapper) predictor).release();
        }
    }

    /**
     * Save raw bytecode into file.
     *
     * @param bytes     class bytecode
     * @param className class name
     * @param dir       save path
     * @throws Exception
     */
    private static void saveClass(final byte[] bytes, final String className, final String dir) throws Exception {
        String fileName = customClassFilePath(className, dir);

        File file = new File(fileName);
        file.getParentFile().mkdirs();
        file.createNewFile();

        try (FileOutputStream fos = new FileOutputStream(fileName)) {
            fos.write(bytes);
        }
    }

    private static String customClassFilePath(final String className, final String dir) {
        return StringUtils.join(dir, File.separator, StringUtils.join(className.split("\\."), File.separator), ".class");
    }

    /**
     * Generated class will start with '_'.
     *
     * @param modelName model name
     * @return class name
     */
    private static String toClassName(final String modelName) {
        return Predictor.PREDICTOR_CLASS_PREFIX + "._" + modelName;
    }

    private static void checkModelName(final String modelName) {
        if (!MODEL_NAME_PATTERN.matcher(modelName).matches()) {
            throw new IllegalArgumentException(String.format("illegal model name: %s, valid character: [a-zA-z0-9_]", modelName));
        }
    }
}
