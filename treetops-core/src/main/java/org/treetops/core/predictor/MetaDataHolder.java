package org.treetops.core.predictor;

import org.treetops.core.model.TreeModel;
import org.treetops.core.model.TreeNode;

import java.util.Objects;

public class MetaDataHolder {

    protected long[][] catBitSet;

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
