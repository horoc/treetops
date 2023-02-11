package org.treetops.core.loader;

import java.io.FileInputStream;
import java.io.InputStream;

public class FileTreeModelLoader extends TreeModelLoader {

    private static class SingletonHolder {
        private static FileTreeModelLoader instance = new FileTreeModelLoader();
    }

    private FileTreeModelLoader() {
    }

    public static FileTreeModelLoader getInstance() {
        return SingletonHolder.instance;
    }

    @Override
    protected InputStream loadStream(String resource) throws Exception {
        return new FileInputStream(resource);
    }
}
