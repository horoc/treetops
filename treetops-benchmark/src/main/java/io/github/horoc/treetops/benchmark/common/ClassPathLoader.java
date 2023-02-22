package io.github.horoc.treetops.benchmark.common;

import io.github.horoc.treetops.core.loader.AbstractLoader;
import java.io.InputStream;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/18
 */
public class ClassPathLoader extends AbstractLoader {

    @Override
    protected InputStream loadStream(final String resource) throws Exception {
        return this.getClass().getResourceAsStream(resource);
    }
}
