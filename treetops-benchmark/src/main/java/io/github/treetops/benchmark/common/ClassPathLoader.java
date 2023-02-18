package io.github.treetops.benchmark.common;

import io.github.treetops.core.loader.AbstractLoader;
import java.io.InputStream;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/18
 */
public class ClassPathLoader extends AbstractLoader {

    @Override
    protected InputStream loadStream(final String resource) throws Exception {
        return this.getClass().getResourceAsStream(resource);
    }
}
