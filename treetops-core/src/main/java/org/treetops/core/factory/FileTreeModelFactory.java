package org.treetops.core.factory;

import org.treetops.core.loader.FileTreeModelLoader;
import org.treetops.core.model.TreeModel;

/**
 * Tree model loading factory
 * <p></p>
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class FileTreeModelFactory {

    /**
     * Load and parse tree model from file
     * @param resource file path
     * @return tree model
     */
    public static TreeModel newInstance(String resource) {
        return FileTreeModelLoader.getInstance().loadModel(resource);
    }
}
