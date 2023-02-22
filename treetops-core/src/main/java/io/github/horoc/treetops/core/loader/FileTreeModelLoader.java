package io.github.horoc.treetops.core.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import org.apache.commons.lang3.Validate;

/**
 * File model loader.
 * <br>
 * @author chenzhou@apache.org
 * created on 2023/2/14
 */
public final class FileTreeModelLoader extends AbstractLoader {

    private FileTreeModelLoader() {
    }

    public static FileTreeModelLoader getInstance() {
        return SingletonHolder.instance;
    }

    @Override
    protected InputStream loadStream(final String resource) throws Exception {
        Validate.notBlank(resource, "model file resource path must not be empty");
        File file = new File(resource);
        if (!file.exists()) {
            throw new FileNotFoundException(String.format("model file not found, resource: %s", resource));
        }
        return new FileInputStream(file);
    }

    private static class SingletonHolder {
        private static FileTreeModelLoader instance = new FileTreeModelLoader();
    }
}
