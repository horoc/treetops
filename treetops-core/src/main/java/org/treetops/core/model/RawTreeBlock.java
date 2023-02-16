package org.treetops.core.model;

import java.util.List;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class RawTreeBlock {
    /**
     * match with model file field:
     * - Tree=%d
     */
    private int tree;
    /**
     * match with model file field:
     * - num_leaves=%d
     */
    private int numLeaves;
    /**
     * match with model file field:
     * - num_cat=%d
     */
    private int numCat;
    /**
     * match with model file field:
     * - split_feature=%f %f ...
     */
    private List<Integer> splitFeature;
    /**
     * match with model file field:
     * - decision_type=%d %d ...
     */
    private List<Integer> decisionType;
    /**
     * match with model file field:
     * - left_child=%d %d ...
     */
    private List<Integer> leftChild;
    /**
     * match with model file field:
     * - left_child=%d %d ...
     */
    private List<Integer> rightChild;
    /**
     * match with model file field:
     * - leaf_value=%f %f ...
     */
    private List<Double> leafValue;
    /**
     * match with model file field:
     * - internal_value=%f %f ...
     */
    private List<Double> internalValue;
    /**
     * match with model file field:
     * - threshold=%f %f ...
     */
    private List<Double> threshold;
    /**
     * match with model file field:
     * - cat_boundaries=%d %d ...
     */
    private List<Integer> catBoundaries;
    /**
     * match with model file field:
     * - cat_threshold=%f %f ...
     */
    private List<Long> catThreshold;

    public int getTree() {
        return tree;
    }

    public void setTree(int tree) {
        this.tree = tree;
    }

    public int getNumLeaves() {
        return numLeaves;
    }

    public void setNumLeaves(int numLeaves) {
        this.numLeaves = numLeaves;
    }

    public int getNumCat() {
        return numCat;
    }

    public void setNumCat(int numCat) {
        this.numCat = numCat;
    }

    public List<Integer> getSplitFeature() {
        return splitFeature;
    }

    public void setSplitFeature(List<Integer> splitFeature) {
        this.splitFeature = splitFeature;
    }

    public List<Integer> getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(List<Integer> decisionType) {
        this.decisionType = decisionType;
    }

    public List<Integer> getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(List<Integer> leftChild) {
        this.leftChild = leftChild;
    }

    public List<Integer> getRightChild() {
        return rightChild;
    }

    public void setRightChild(List<Integer> rightChild) {
        this.rightChild = rightChild;
    }

    public List<Double> getLeafValue() {
        return leafValue;
    }

    public void setLeafValue(List<Double> leafValue) {
        this.leafValue = leafValue;
    }

    public List<Double> getInternalValue() {
        return internalValue;
    }

    public void setInternalValue(List<Double> internalValue) {
        this.internalValue = internalValue;
    }

    public List<Double> getThreshold() {
        return threshold;
    }

    public void setThreshold(List<Double> threshold) {
        this.threshold = threshold;
    }

    public List<Integer> getCatBoundaries() {
        return catBoundaries;
    }

    public void setCatBoundaries(List<Integer> catBoundaries) {
        this.catBoundaries = catBoundaries;
    }

    public List<Long> getCatThreshold() {
        return catThreshold;
    }

    public void setCatThreshold(List<Long> catThreshold) {
        this.catThreshold = catThreshold;
    }
}
