package io.github.horoc.treetops.core.model;

/**
 * @author chenzhou@apache.org
 * created on 2023/2/14
 */
public enum MissingType {
    /**
     * decision type mask for default None value.
     */
    None(0),
    /**
     * decision type mask for default Zero value.
     */
    Zero(1),
    /**
     * decision type mask for default Nan value.
     */
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
