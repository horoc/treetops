package io.github.treetops.core.predictor;

import io.github.treetops.core.model.TreeModel;
import io.github.treetops.core.model.TreeNode;
import java.util.Objects;

/**
 * Meta data holder for Predictor.
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class MetaDataHolder {

    /**
     * category bit threshold used in process of category node decision.
     */
    private long[][] catBitSet;

    public void initialize(TreeModel model) {
        if (model.isContainsCatNode()) {
            catBitSet = new long[model.getTrees().size()][];
            for (int i = 0; i < catBitSet.length; i++) {
                TreeNode root = model.getTrees().get(i);
                if (Objects.isNull(root.getCatThreshold()) || root.getCatThreshold().isEmpty()) {
                    continue;
                }
                catBitSet[i] = root.getCatThreshold().stream().mapToLong(l -> l).toArray();
            }
        }
    }

    /**
     * Refer to include/LightGBM/utils/common.h#FindInBitsets.
     *
     * @param index tree index
     * @param begin begin
     * @param n     length
     * @param val   feature value
     * @return is find bit
     */
    protected boolean findCatBitset(int index, int begin, int n, double val) {
        int pos = (int) val;
        int i1 = pos / 32;
        if (i1 >= n) {
            return false;
        }
        int i2 = pos % 32;
        return ((catBitSet[index][i1 + begin] >> i2) & 1) > 0;
    }
}
