package org.treetops.core.generator;

import org.treetops.core.model.TreeModel;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public interface Generator {

    Class<?> defineClassFromCode(String name, byte[] code);

    byte[] generateCode(String name, TreeModel model);
}
