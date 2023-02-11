package org.treetops.core.generator;


import org.treetops.core.model.TreeModel;

public interface Generator {

    Class<?> defineClassFromCode(String name, byte[] code);

    byte[] generateCode(String name, TreeModel model);
}
