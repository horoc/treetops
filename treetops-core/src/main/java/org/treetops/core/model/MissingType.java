package org.treetops.core.model;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public enum MissingType {
    None(0),
    Zero(1),
    Nan(2);

    private final int mask;

    MissingType(int mask) {
        this.mask = mask;
    }

    public int getMask() {
        return mask;
    }

    public static MissingType ofMask(int mask) {
        for (MissingType type : MissingType.values()) {
            if (type.getMask() == mask) {
                return type;
            }
        }
        return null;
    }
}
