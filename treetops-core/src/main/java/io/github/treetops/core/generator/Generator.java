package io.github.treetops.core.generator;

import io.github.treetops.core.model.TreeModel;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public interface Generator {

    /**
     * Load generated class bytecode into memory
     *
     * @param className class name
     * @param code      bytecode data
     * @return defined class
     */
    Class<?> defineClassFromCode(String className, byte[] code);

    /**
     * Generate predictor bytecode
     *
     * @param className class name
     * @param model     tree model
     * @return bytecode data
     */
    byte[] generateCode(String className, TreeModel model);
}
