package io.github.treetops.core.loader;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * File model loader.
 * <p></p>
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public final class FileTreeModelLoader extends AbstractLoader {

    private FileTreeModelLoader() {
    }

    public static FileTreeModelLoader getInstance() {
        return SingletonHolder.instance;
    }

    @Override
    protected InputStream loadStream(String resource) throws Exception {
        return new FileInputStream(resource);
    }

    private static class SingletonHolder {
        private static FileTreeModelLoader instance = new FileTreeModelLoader();
    }
}
