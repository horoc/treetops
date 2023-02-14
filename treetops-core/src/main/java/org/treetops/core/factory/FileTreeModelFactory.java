package org.treetops.core.factory;

import org.treetops.core.loader.FileTreeModelLoader;
import org.treetops.core.model.TreeModel;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class FileTreeModelFactory {

    public static TreeModel newInstance(String resource) {
        return FileTreeModelLoader.getInstance().loadModel(resource);
    }
}
