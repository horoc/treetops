package io.github.horoc.treetops.core.parser;

import io.github.horoc.treetops.core.factory.ObjectiveDecoratorFactory;
import io.github.horoc.treetops.core.model.RawTreeBlock;
import io.github.horoc.treetops.core.model.TreeModel;
import io.github.horoc.treetops.core.model.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/**
 * Parse String model info into TreeModel instance.
 *
 * @author chenzhou@apache.org
 * created on 2023/2/14
 */
public class TreeModelParser {

    /**
     * Refer to official library: microsoft/LightGBM/include/LightGBM/tree.h#kCategoricalMask.
     */
    private static final int CATEGORICAL_MASK = 1;

    /**
     * Refer to official library: microsoft/LightGBM/include/LightGBM/tree.h#kDefaultLeftMask.
     */
    private static final int DEFAULT_LEFT_MASK = 2;

    /**
     * Model file config separator.
     */
    private static final String CONFIG_SEPARATOR = " ";

    /**
     * Parse workflow: <br>
     * 1. iterator each line to find meta block header or tree block header <br>
     * 2. parse different block into tree model instance <br>
     *
     * @param rawLines raw data
     * @return tree model instance
     */
    public static TreeModel parseTreeModel(final List<String> rawLines) {
        Validate.notNull(rawLines);

        TreeModel treeModel = new TreeModel();
        int curLineIndex = 0;
        while (curLineIndex < rawLines.size()) {
            String line = rawLines.get(curLineIndex);
            if (StringUtils.isBlank(line)) {
                curLineIndex++;
                continue;
            }
            // we only need tree info and meta block info
            if (isTreeBlockHeader(line)) {
                TreeNode tree = new TreeNode();
                curLineIndex = initTreeBlock(tree, rawLines, curLineIndex);
                treeModel.getTrees().add(tree);
                // mark model contains category node, need to initialize threshold data later
                if (Objects.nonNull(tree.getCatThreshold()) && !tree.getCatThreshold().isEmpty()) {
                    treeModel.setContainsCatNode(true);
                }
            } else if (isMetaInfoBlockHeader(line)) {
                // skip first line of meta info block
                curLineIndex = initMetaBlock(treeModel, rawLines, curLineIndex + 1);
            }
            curLineIndex++;
        }
        checkTreeBlock(treeModel);
        return treeModel;
    }

    /**
     * load meta block info into model.
     *
     * @param treeModel     tree model
     * @param rawStingLines raw string lines
     * @param offset        read offset
     * @return next offset
     */
    private static int initMetaBlock(TreeModel treeModel, final List<String> rawStingLines, int offset) {
        Map<String, String> rawDataMap = new HashMap<>();
        final int nextOffset = parseRawKeyValueMap(rawStingLines, rawDataMap, offset);
        convertAndSetField("objective", rawDataMap, TreeModelParser::parseObjectiveType, treeModel::setObjectiveType);
        convertAndSetField("objective", rawDataMap, TreeModelParser::parseObjectiveConfig, treeModel::setObjectiveConfig);
        convertAndSetField("num_class", rawDataMap, Integer::valueOf, treeModel::setNumClass);
        convertAndSetField("max_feature_idx", rawDataMap, Integer::valueOf, treeModel::setMaxFeatureIndex);
        convertAndSetField("num_tree_per_iteration", rawDataMap, Integer::valueOf, treeModel::setNumberTreePerIteration);
        return nextOffset;
    }

    /**
     * load tree block info into model.
     *
     * @param root          tree node root
     * @param rawStingLines raw string lines
     * @param offset        read offset
     * @return next offset
     */
    private static int initTreeBlock(TreeNode root, final List<String> rawStingLines, int offset) {
        Map<String, String> rawDataMap = new HashMap<>();
        final int nextOffset = parseRawKeyValueMap(rawStingLines, rawDataMap, offset);

        RawTreeBlock block = new RawTreeBlock();
        convertAndSetField("Tree", rawDataMap, Integer::valueOf, block::setTree);
        convertAndSetField("num_leaves", rawDataMap, Integer::valueOf, block::setNumLeaves);
        convertAndSetField("num_cat", rawDataMap, Integer::valueOf, block::setNumCat);
        convertAndSetField("split_feature", rawDataMap, val -> fromStringToList(val, Integer::valueOf), block::setSplitFeature);
        convertAndSetField("decision_type", rawDataMap, val -> fromStringToList(val, Integer::valueOf), block::setDecisionType);
        convertAndSetField("left_child", rawDataMap, val -> fromStringToList(val, Integer::valueOf), block::setLeftChild);
        convertAndSetField("right_child", rawDataMap, val -> fromStringToList(val, Integer::valueOf), block::setRightChild);
        convertAndSetField("leaf_value", rawDataMap, val -> fromStringToList(val, Double::valueOf), block::setLeafValue);
        convertAndSetField("internal_value", rawDataMap, val -> fromStringToList(val, Double::valueOf), block::setInternalValue);
        convertAndSetField("threshold", rawDataMap, val -> fromStringToList(val, Double::valueOf), block::setThreshold);
        convertAndSetField("cat_boundaries", rawDataMap, val -> fromStringToList(val, Integer::valueOf), block::setCatBoundaries);
        convertAndSetField("cat_threshold", rawDataMap, val -> fromStringToList(val, Long::valueOf), block::setCatThreshold);

        // init all nodes
        int treeSize = block.getLeftChild().size();
        List<TreeNode> treeNodes = new ArrayList<>(treeSize);
        treeNodes.add(0, root);
        for (int i = 1; i < treeSize; i++) {
            treeNodes.add(i, new TreeNode(i));
        }
        treeNodes.forEach(o -> initTreeSingleNode(o, block));

        // link all nodes
        for (int i = 0; i < treeSize; i++) {
            linkTreeNode(treeNodes.get(i), treeNodes, block);
        }

        // sort node array by index
        treeNodes.sort((a, b) -> {
            int i = a.getNodeIndex();
            int j = b.getNodeIndex();
            if (i >= 0 && j >= 0) {
                return i - j;
            } else {
                return j - i;
            }
        });

        return nextOffset;
    }

    /**
     * load tree node block info into model.
     *
     * @param node  tree node
     * @param block raw block data
     */
    private static void initTreeSingleNode(TreeNode node, final RawTreeBlock block) {
        int nodeIndex = node.getNodeIndex();
        node.setLeaf(false);
        node.setTreeIndex(block.getTree());
        node.setDecisionType(block.getDecisionType().get(nodeIndex));
        node.setCategoryNode(isCategoryNode(block.getDecisionType().get(nodeIndex)));
        node.setDefaultLeftDecision(isDefaultLeftDecisionNode(block.getDecisionType().get(nodeIndex)));
        node.setSplitFeatures(block.getSplitFeature());
        node.setCatThreshold(block.getCatThreshold());
        if (node.isCategoryNode()) {
            node.setCatBoundaryBegin(block.getThreshold().get(nodeIndex).intValue());
            node.setCatBoundaryEnd(node.getCatBoundaryBegin() + 1);
        } else {
            node.setThreshold(block.getThreshold().get(nodeIndex));
        }
    }

    private static void linkTreeNode(TreeNode node, List<TreeNode> treeNodes, final RawTreeBlock block) {
        int leftIndex = block.getLeftChild().get(node.getNodeIndex());
        if (leftIndex < 0) {
            TreeNode leftLeaf = new TreeNode(node.getTreeIndex(), leftIndex);
            leftLeaf.setLeaf(true);
            leftLeaf.setLeafValue(block.getLeafValue().get(-leftIndex - 1));
            node.setLeftNode(leftLeaf);
            treeNodes.add(leftLeaf);
        } else {
            node.setLeftNode(treeNodes.get(leftIndex));
        }

        int rightIndex = block.getRightChild().get(node.getNodeIndex());
        if (rightIndex < 0) {
            TreeNode rightLeaf = new TreeNode(node.getTreeIndex(), rightIndex);
            rightLeaf.setLeaf(true);
            rightLeaf.setLeafValue(block.getLeafValue().get(-rightIndex - 1));
            node.setRightNode(rightLeaf);
            treeNodes.add(rightLeaf);
        } else {
            node.setRightNode(treeNodes.get(rightIndex));
        }

        node.setAllNodes(treeNodes);
    }

    private static int parseRawKeyValueMap(List<String> metaInfos, Map<String, String> rawDataMap, int offset) {
        int nextOffset = offset;
        for (; nextOffset < metaInfos.size(); nextOffset++) {
            String line = metaInfos.get(nextOffset);
            if (StringUtils.isBlank(line)) {
                return nextOffset;
            }
            String[] sp = line.split("=");
            if (sp.length != 2) {
                throw new RuntimeException(String.format("try to parse tree model failed, invalid key-value content %s", line));
            }
            rawDataMap.put(sp[0], sp[1]);
        }
        return nextOffset;
    }

    private static boolean isCategoryNode(int decisionType) {
        return (decisionType & CATEGORICAL_MASK) > 0;
    }

    private static boolean isDefaultLeftDecisionNode(int decisionType) {
        return (decisionType & DEFAULT_LEFT_MASK) > 0;
    }

    private static boolean isTreeBlockHeader(final String line) {
        return StringUtils.isNoneBlank(line) && line.startsWith("Tree=");
    }

    private static boolean isMetaInfoBlockHeader(final String line) {
        return StringUtils.isNoneBlank(line) && "tree".equals(line);
    }

    private static String parseObjectiveType(final String objective) {
        if (StringUtils.isNotBlank(objective)) {
            return objective.split(CONFIG_SEPARATOR)[0];
        }
        return StringUtils.EMPTY;
    }

    private static String parseObjectiveConfig(final String objective) {
        if (StringUtils.isNotBlank(objective)) {
            String[] sp = objective.split(CONFIG_SEPARATOR);
            if (sp.length > 1) {
                return sp[1];
            }
        }
        return StringUtils.EMPTY;
    }

    private static <T> List<T> fromStringToList(final String str, Function<String, T> converter) {
        String[] splits = str.split(CONFIG_SEPARATOR);
        List<T> ret = new ArrayList<>(splits.length);
        for (String val : splits) {
            ret.add(converter.apply(val));
        }
        return ret;
    }

    private static <T> void convertAndSetField(final String key, Map<String, String> rawDataMap,
                                               Function<String, T> converter, Consumer<T> setter) {
        String rawValue = rawDataMap.get(key);
        if (StringUtils.isNoneBlank(rawValue)) {
            try {
                setter.accept(converter.apply(rawValue));
            } catch (Throwable e) {
                throw new RuntimeException(String.format("parsing tree model file error, "
                    + "try to convert field key: , value: ", key, rawValue), e);
            }
        }
    }

    private static void checkTreeBlock(final TreeModel treeModel) {
        Validate.notNull(treeModel,
            "parsing tree model failed, can not find any valid block");

        Validate.notEmpty(treeModel.getTrees(),
            "parsing tree model failed, can not find any valid block");

        Validate.isTrue(treeModel.getNumClass() > 0,
            "parsing tree model failed, invalid meta info, num class must be positive");

        Validate.isTrue(treeModel.getMaxFeatureIndex() >= 0,
            "parsing tree model failed, invalid meta info, max feature index can not be negative");

        Validate.isTrue(treeModel.getNumberTreePerIteration() > 0,
            "parsing tree model failed, invalid meta info, number tree per iteration must be positive");

        Validate.isTrue(ObjectiveDecoratorFactory.isValidObjectiveType(treeModel.getObjectiveType()),
            "parsing tree model failed, invalid meta info, objective type %s is not supported", treeModel.getObjectiveType());
    }
}
