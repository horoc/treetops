package org.treetops.core.loader;


import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.treetops.core.model.TreeModel;
import org.treetops.core.parser.TreeModelParser;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public abstract class TreeModelLoader {

    protected abstract InputStream loadStream(String resource) throws Exception;

    public TreeModel loadModel(String resource) {
        List<String> lines = null;
        try (InputStream stream = loadStream(resource)) {
            lines = IOUtils.readLines(stream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // TODO error type
            throw new RuntimeException("");
        }
        return TreeModelParser.parseTreeModel(lines);
    }
}
