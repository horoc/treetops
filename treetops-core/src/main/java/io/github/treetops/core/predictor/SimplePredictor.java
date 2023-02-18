package io.github.treetops.core.predictor;

import io.github.treetops.core.model.MissingType;
import io.github.treetops.core.model.TreeModel;
import io.github.treetops.core.model.TreeNode;

/**
 * Simple predictor implementation, follow the LightGBM cpp implementation.
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class SimplePredictor extends MetaDataHolder implements Predictor {

    private static final double K_ZERO_THRESHOLD = 1e-35f;

    private final TreeModel treeModel;

    public SimplePredictor(TreeModel treeModel) {
        this.treeModel = treeModel;
    }

    @Override
    public double[] predictRaw(double[] features) {
        double[] ret = new double[treeModel.getNumClass()];
        for (TreeNode root : treeModel.getTrees()) {
            ret[root.getTreeIndex() % treeModel.getNumClass()] += this.decision(root, features);
        }
        return ret;
    }

    /**
     * Refer to official library: microsoft/LightGBM/include/LightGBM/tree.h#Tree::Decision.
     * <p></p>
     *
     * @param treeNode tree node meta data
     * @param features input data
     * @return decision value of this node
     */
    private double decision(TreeNode treeNode, double[] features) {
        if (treeNode.isLeaf()) {
            return treeNode.getLeafValue();
        }

        if (treeNode.isCategoryNode()) {
            return categoricalDecision(treeNode, features);
        } else {
            return numericalDecision(treeNode, features);
        }
    }

    /**
     * Refer to official library: microsoft/LightGBM/include/LightGBM/tree.h#Tree::NumericalDecision.
     * <p></p>
     *
     * @param treeNode tree node meta data
     * @param features input data
     * @return decision value of this node
     */
    private double numericalDecision(TreeNode treeNode, double[] features) {
        MissingType missingType = MissingType.ofMask(treeNode.getDecisionType());
        double threshold = treeNode.getThreshold();
        double feature = features[treeNode.getSplitFeatures().get(treeNode.getNodeIndex())];
        if (Double.isNaN(feature) && missingType != MissingType.Nan) {
            feature = 0.0;
        }

        boolean isZeroMiss = missingType == MissingType.Zero && isZero(feature);
        boolean isNanMiss = missingType == MissingType.Nan && Double.isNaN(feature);
        if (isZeroMiss || isNanMiss) {
            if (treeNode.isDefaultLeftDecision()) {
                return decision(treeNode.getLeftNode(), features);
            } else {
                return decision(treeNode.getRightNode(), features);
            }
        }

        if (feature <= threshold) {
            return decision(treeNode.getLeftNode(), features);
        } else {
            return decision(treeNode.getRightNode(), features);
        }
    }

    /**
     * Refer to official library: microsoft/LightGBM/include/LightGBM/tree.h#Tree::CategoricalDecision.
     * <p></p>
     *
     * @param treeNode tree node meta data
     * @param features input data
     * @return decision value of this node
     */
    private double categoricalDecision(TreeNode treeNode, double[] features) {
        double feature = features[treeNode.getSplitFeatures().get(treeNode.getNodeIndex())];
        if (Double.isNaN(feature)) {
            return decision(treeNode.getRightNode(), features);
        } else {
            int val = (int) feature;
            if (val < 0) {
                return decision(treeNode.getRightNode(), features);
            }
        }
        if (findCatBitset(treeNode.getTreeIndex(), treeNode.getCatBoundaryBegin(),
            treeNode.getCatBoundaryEnd() - treeNode.getCatBoundaryBegin(), feature)) {
            return decision(treeNode.getLeftNode(), features);
        }
        return decision(treeNode.getRightNode(), features);
    }

    private boolean isZero(double val) {
        return val >= -K_ZERO_THRESHOLD && val <= K_ZERO_THRESHOLD;
    }
}
