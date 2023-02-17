package io.github.treetops.benchmark.common;

import java.io.InputStream;

import io.github.treetops.core.loader.AbstractLoader;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/18
 */
public class ClassPathLoader extends AbstractLoader {

    @Override
    protected InputStream loadStream(String resource) throws Exception {
        return this.getClass().getResourceAsStream(resource);
    }
}
