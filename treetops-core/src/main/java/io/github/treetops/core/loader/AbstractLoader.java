package io.github.treetops.core.loader;

import io.github.treetops.core.model.TreeModel;
import io.github.treetops.core.parser.TreeModelParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public abstract class AbstractLoader {

    /**
     * Get input stream from corresponding resource path.
     *
     * @param resource resource path
     * @return InputStream from resource
     * @throws Exception
     */
    protected abstract InputStream loadStream(String resource) throws Exception;

    /**
     * Load model from resource based on loadStream implementation, and parse into {@link TreeModel}.
     *
     * @param resource resource pah
     * @return TreeModel instance
     */
    public TreeModel loadModel(final String resource) {
        List<String> lines;
        try (InputStream stream = loadStream(resource)) {
            lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(String.format("fail to load model from resource: %s", resource), e);
        }
        return TreeModelParser.parseTreeModel(lines);
    }
}
