package io.github.treetops.core.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class TreeModel {

    private int numClass;

    private int numberTreePerIteration;

    private int maxFeatureIndex;

    private boolean containsCatNode;

    private String objectiveType;

    private String objectiveConfig;

    private List<TreeNode> trees = new ArrayList<>();

    public int getNumClass() {
        return numClass;
    }

    public void setNumClass(int numClass) {
        this.numClass = numClass;
    }

    public int getNumberTreePerIteration() {
        return numberTreePerIteration;
    }

    public void setNumberTreePerIteration(int numberTreePerIteration) {
        this.numberTreePerIteration = numberTreePerIteration;
    }

    public int getMaxFeatureIndex() {
        return maxFeatureIndex;
    }

    public void setMaxFeatureIndex(int maxFeatureIndex) {
        this.maxFeatureIndex = maxFeatureIndex;
    }

    public boolean isContainsCatNode() {
        return containsCatNode;
    }

    public void setContainsCatNode(boolean containsCatNode) {
        this.containsCatNode = containsCatNode;
    }

    public String getObjectiveType() {
        return objectiveType;
    }

    public void setObjectiveType(String objectiveType) {
        this.objectiveType = objectiveType;
    }

    public String getObjectiveConfig() {
        return objectiveConfig;
    }

    public void setObjectiveConfig(String objectiveConfig) {
        this.objectiveConfig = objectiveConfig;
    }

    public List<TreeNode> getTrees() {
        return trees;
    }

    public void setTrees(List<TreeNode> trees) {
        this.trees = trees;
    }
}
