package io.github.treetops.core.factory;

import io.github.treetops.core.loader.FileTreeModelLoader;
import io.github.treetops.core.model.TreeModel;
import org.apache.commons.lang3.Validate;

/**
 * Tree model loading factory.
 * <p></p>
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class FileTreeModelFactory {

    /**
     * Load and parse tree model from file.
     *
     * @param resource file path
     * @return tree model
     */
    public static TreeModel newInstance(final String resource) {
        Validate.notBlank(resource, "model file resource path must not be empty");
        return FileTreeModelLoader.getInstance().loadModel(resource);
    }
}
