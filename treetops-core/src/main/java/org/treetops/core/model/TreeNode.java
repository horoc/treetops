package org.treetops.core.model;

import java.util.List;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class TreeNode {
    private int treeIndex;
    private int nodeIndex;
    private boolean isCategoryNode;
    private boolean isDefaultLeftDecision;
    private int decisionType;

    private List<Integer> splitFeatures;
    private double threshold;
    private int catBoundaryBegin;
    private int catBoundaryEnd;
    private List<Long> catThreshold;

    private TreeNode leftNode;
    private TreeNode rightNode;

    private boolean isLeaf;
    private double leafValue;

    private List<TreeNode> allNodes;

    public TreeNode() {
    }

    public TreeNode(int treeIndex, int nodeIndex) {
        this.treeIndex = treeIndex;
        this.nodeIndex = nodeIndex;
    }

    public TreeNode(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public int getTreeIndex() {
        return treeIndex;
    }

    public void setTreeIndex(int treeIndex) {
        this.treeIndex = treeIndex;
    }

    public int getNodeIndex() {
        return nodeIndex;
    }

    public void setNodeIndex(int nodeIndex) {
        this.nodeIndex = nodeIndex;
    }

    public boolean isCategoryNode() {
        return isCategoryNode;
    }

    public void setCategoryNode(boolean categoryNode) {
        isCategoryNode = categoryNode;
    }

    public boolean isDefaultLeftDecision() {
        return isDefaultLeftDecision;
    }

    public void setDefaultLeftDecision(boolean defaultLeftDecision) {
        isDefaultLeftDecision = defaultLeftDecision;
    }

    public int getDecisionType() {
        return decisionType;
    }

    public void setDecisionType(int decisionType) {
        this.decisionType = decisionType;
    }

    public List<Integer> getSplitFeatures() {
        return splitFeatures;
    }

    public void setSplitFeatures(List<Integer> splitFeatures) {
        this.splitFeatures = splitFeatures;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getCatBoundaryBegin() {
        return catBoundaryBegin;
    }

    public void setCatBoundaryBegin(int catBoundaryBegin) {
        this.catBoundaryBegin = catBoundaryBegin;
    }

    public int getCatBoundaryEnd() {
        return catBoundaryEnd;
    }

    public void setCatBoundaryEnd(int catBoundaryEnd) {
        this.catBoundaryEnd = catBoundaryEnd;
    }

    public List<Long> getCatThreshold() {
        return catThreshold;
    }

    public void setCatThreshold(List<Long> catThreshold) {
        this.catThreshold = catThreshold;
    }

    public TreeNode getLeftNode() {
        return leftNode;
    }

    public void setLeftNode(TreeNode leftNode) {
        this.leftNode = leftNode;
    }

    public TreeNode getRightNode() {
        return rightNode;
    }

    public void setRightNode(TreeNode rightNode) {
        this.rightNode = rightNode;
    }

    public boolean isLeaf() {
        return isLeaf;
    }

    public void setLeaf(boolean leaf) {
        isLeaf = leaf;
    }

    public double getLeafValue() {
        return leafValue;
    }

    public void setLeafValue(double leafValue) {
        this.leafValue = leafValue;
    }

    public List<TreeNode> getAllNodes() {
        return allNodes;
    }

    public void setAllNodes(List<TreeNode> allNodes) {
        this.allNodes = allNodes;
    }
}
