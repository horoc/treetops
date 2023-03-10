package io.github.horoc.treetops.core.loader;

import io.github.horoc.treetops.core.model.TreeModel;
import io.github.horoc.treetops.core.parser.TreeModelParser;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/14
 */
public abstract class AbstractLoader {

    /**
     * Get input stream from corresponding resource path.
     *
     * @param resource resource path
     * @return InputStream from resource
     * @throws Exception exception while loading stream from resource
     */
    protected abstract InputStream loadStream(String resource) throws Exception;

    /**
     * Load model from resource based on loadStream implementation, and parse into {@link TreeModel}.
     *
     * @param resource resource pah
     * @return TreeModel instance
     */
    public TreeModel loadModel(final String resource) {
        Validate.notBlank(resource, "model resource path must not be empty");
        List<String> lines;
        try (InputStream stream = loadStream(resource)) {
            lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException(String.format("fail to load model from resource: %s", resource), e);
        }
        return TreeModelParser.parseTreeModel(lines);
    }
}
