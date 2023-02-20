package io.github.horoc.treetops.core.generator;

import io.github.horoc.treetops.core.model.MissingType;
import io.github.horoc.treetops.core.model.TreeModel;
import io.github.horoc.treetops.core.model.TreeNode;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * Predictor class generator based on asm framework.
 * <p></p>
 *
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public final class PredictorClassGenerator extends ClassLoader implements Generator, Opcodes {

    private static final int FEATURE_PARAMETER_INDEX = 1;

    private static final double K_ZERO_THRESHOLD = 1e-35f;

    private static final String INIT = "<init>";

    private static final String TREE_METHOD_PREFIX = "tree_";

    private static final String OBJECT_INTERNAL_NAME = "java/lang/Object";

    private static final String PREDICTOR_INTERNAL_NAME = "io/github/horoc/treetops/core/predictor/Predictor";

    private static final String META_DATA_HOLDER_INTERNAL_NAME = "io/github/horoc/treetops/core/predictor/MetaDataHolder";

    private static final String PREDICT_METHOD = "predictRaw";

    private static final String FIND_CAT_BIT_SET_METHOD = "findCatBitset";

    private PredictorClassGenerator() {
    }

    /**
     * We can not maintain a singleton instance of generator here, <br/>
     * since we want jvm to unload class which would be no longer used.
     *
     * @return predictor instance
     */
    public static PredictorClassGenerator getInstance() {
        return new PredictorClassGenerator();
    }

    @Override
    public Class<?> defineClassFromCode(final String className, final byte[] code) {
        return this.defineClass(className, code, 0, code.length);
    }

    @Override
    public byte[] generateCode(final String className, final TreeModel model) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new CheckClassAdapter(cw);
        String internalClassName = toInternalName(className);

        // define class
        cv.visit(V1_8, ACC_PUBLIC | ACC_SUPER, toInternalName(internalClassName), null, getSuperName(model),
            new String[] {PREDICTOR_INTERNAL_NAME});

        // define init method
        addInitMethod(cv, model);

        // tree decision method
        // description : private double tree_[%tree_index](double[] features);
        model.getTrees().forEach(t -> addTreeMethod(cv, internalClassName, t));

        // prediction method
        addPredictionMethod(cv, internalClassName, model);

        cv.visitEnd();
        return cw.toByteArray();
    }

    /**
     * Define init method.
     *
     * @param cv    class visitor
     * @param model model config
     */
    private void addInitMethod(ClassVisitor cv, final TreeModel model) {
        MethodVisitor methodVisitor = simpleVisitMethod(cv, ACC_PUBLIC, INIT, "()V");
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        if (model.isContainsCatNode()) {
            methodVisitor.visitMethodInsn(INVOKESPECIAL, META_DATA_HOLDER_INTERNAL_NAME, INIT, "()V", false);
        } else {
            methodVisitor.visitMethodInsn(INVOKESPECIAL, OBJECT_INTERNAL_NAME, INIT, "()V", false);
        }
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    private void addPredictionMethod(ClassVisitor cv, final String className, final TreeModel model) {
        MethodVisitor methodVisitor = simpleVisitMethod(cv, ACC_PUBLIC, PREDICT_METHOD, "([D)[D");
        methodVisitor.visitCode();

        methodVisitor.visitLdcInsn(model.getNumClass());
        methodVisitor.visitIntInsn(NEWARRAY, T_DOUBLE);
        methodVisitor.visitVarInsn(ASTORE, 2);

        for (int i = 0; i < model.getTrees().size(); i++) {
            TreeNode root = model.getTrees().get(i);
            methodVisitor.visitVarInsn(ALOAD, 2);
            methodVisitor.visitLdcInsn(root.getTreeIndex() % model.getNumClass());
            methodVisitor.visitInsn(DUP2);
            methodVisitor.visitInsn(DALOAD);
            methodVisitor.visitVarInsn(ALOAD, 0);
            methodVisitor.visitVarInsn(ALOAD, 1);
            methodVisitor.visitMethodInsn(INVOKESPECIAL, className, TREE_METHOD_PREFIX + root.getTreeIndex(), "([D)D", false);
            methodVisitor.visitInsn(DADD);
            methodVisitor.visitInsn(DASTORE);
        }

        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    private void addTreeMethod(ClassVisitor cv, final String className, final TreeNode root) {
        MethodVisitor methodVisitor = simpleVisitMethod(cv, ACC_PRIVATE, TREE_METHOD_PREFIX + root.getTreeIndex(), "([D)D");
        methodVisitor.visitCode();

        Map<Integer, Label> labels = root.getAllNodes().stream().collect(Collectors.toMap(TreeNode::getNodeIndex, o -> new Label()));
        root.getAllNodes().forEach(node -> defineNodeBlock(methodVisitor, node, className, labels));

        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    private void defineNodeBlock(MethodVisitor methodVisitor, final TreeNode node, final String className,
                                 final Map<Integer, Label> labels) {
        if (node.isLeaf()) {
            defineLeafNodeBlock(methodVisitor, node, labels);
            return;
        }

        if (node.isCategoryNode()) {
            defineCategoryNodeBlock(methodVisitor, node, className, labels);
        } else {
            defineNumericalNodeBlock(methodVisitor, node, labels);
        }
    }

    private void defineLeafNodeBlock(MethodVisitor methodVisitor, final TreeNode node, final Map<Integer, Label> labels) {
        int nodeIndex = node.getNodeIndex();
        methodVisitor.visitLabel(labels.get(nodeIndex));
        methodVisitor.visitLdcInsn(new Double(node.getLeafValue()));
        methodVisitor.visitInsn(DRETURN);
    }

    @SuppressWarnings("Duplicates")
    private void defineNumericalNodeBlock(MethodVisitor methodVisitor, final TreeNode node, final Map<Integer, Label> labels) {
        int nodeIndex = node.getNodeIndex();
        methodVisitor.visitLabel(labels.get(nodeIndex));

        // load feature
        loadFeatureByIndex(methodVisitor, node.getSplitFeatures().get(nodeIndex));
        methodVisitor.visitVarInsn(DSTORE, 2);

        // if missing_type != nan and feature is nan, set feature to zero
        MissingType missingType = MissingType.ofMask(node.getDecisionType());
        if (missingType != MissingType.Nan) {
            methodVisitor.visitVarInsn(DLOAD, 2);
            methodVisitor.visitVarInsn(DLOAD, 2);
            Label label = new Label();
            methodVisitor.visitJumpInsn(IFNE, labels.get(node.getRightNode().getNodeIndex()));
            methodVisitor.visitLabel(label);
            // set feature to zero
            methodVisitor.visitInsn(DCONST_0);
            methodVisitor.visitVarInsn(DSTORE, 2);
        }

        // if missingType == zero and feature is zero
        if (missingType == MissingType.Zero) {
            Label label = new Label();
            // if feature < -1e-35, not zero, jump to continue
            methodVisitor.visitVarInsn(DLOAD, 2);
            methodVisitor.visitLdcInsn(new Double(-K_ZERO_THRESHOLD));
            methodVisitor.visitInsn(DCMPL);
            methodVisitor.visitJumpInsn(IFLT, label);

            // if feature > 1e-35, not zero, jump to continue
            methodVisitor.visitVarInsn(DLOAD, 2);
            methodVisitor.visitLdcInsn(new Double(K_ZERO_THRESHOLD));
            methodVisitor.visitInsn(DCMPG);
            methodVisitor.visitJumpInsn(IFGT, label);

            // if feature is zero, jump to next node
            if (node.isDefaultLeftDecision()) {
                methodVisitor.visitJumpInsn(GOTO, labels.get(node.getLeftNode().getNodeIndex()));
            } else {
                methodVisitor.visitJumpInsn(GOTO, labels.get(node.getRightNode().getNodeIndex()));
            }

            // continue
            methodVisitor.visitLabel(label);
        }

        // if missingType == nan and feature is nan
        if (missingType == MissingType.Nan) {
            Label label = new Label();
            // if feature is not nan, jump to continue
            methodVisitor.visitVarInsn(DLOAD, 2);
            methodVisitor.visitVarInsn(DLOAD, 2);
            methodVisitor.visitInsn(DCMPL);
            methodVisitor.visitJumpInsn(IFEQ, label);

            // if feature is nan, jump to next node
            if (node.isDefaultLeftDecision()) {
                methodVisitor.visitJumpInsn(GOTO, labels.get(node.getLeftNode().getNodeIndex()));
            } else {
                methodVisitor.visitJumpInsn(GOTO, labels.get(node.getRightNode().getNodeIndex()));
            }

            // continue
            methodVisitor.visitLabel(label);
        }

        // compare to threshold
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitLdcInsn(new Double(node.getThreshold()));
        methodVisitor.visitInsn(DCMPG);
        // feature > threshold, jump to right
        methodVisitor.visitJumpInsn(IFGE, labels.get(node.getRightNode().getNodeIndex()));
        // feature <= threshold, jump to left
        methodVisitor.visitJumpInsn(GOTO, labels.get(node.getLeftNode().getNodeIndex()));
    }

    private void defineCategoryNodeBlock(MethodVisitor methodVisitor, final TreeNode node, final String className,
                                         final Map<Integer, Label> labels) {
        int nodeIndex = node.getNodeIndex();
        methodVisitor.visitLabel(labels.get(nodeIndex));

        // load feature
        loadFeatureByIndex(methodVisitor, node.getSplitFeatures().get(nodeIndex));
        methodVisitor.visitVarInsn(DSTORE, 2);

        // if feature isNaN, jump to right child node
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitInsn(DCMPL);
        methodVisitor.visitJumpInsn(IFNE, labels.get(node.getRightNode().getNodeIndex()));

        // if feature < 0, jump to right child node
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitInsn(DCONST_0);
        methodVisitor.visitInsn(DCMPG);
        methodVisitor.visitJumpInsn(IFLT, labels.get(node.getRightNode().getNodeIndex()));

        // if findInBitset, jump to right child node
        methodVisitor.visitVarInsn(ALOAD, 0);
        methodVisitor.visitLdcInsn(node.getTreeIndex());
        methodVisitor.visitLdcInsn(node.getCatBoundaryBegin());
        methodVisitor.visitLdcInsn(node.getCatBoundaryEnd() - node.getCatBoundaryBegin());
        methodVisitor.visitVarInsn(DLOAD, 2);
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, className, FIND_CAT_BIT_SET_METHOD, "(IIID)Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels.get(node.getLeftNode().getNodeIndex()));

        // others, jump left child node
        methodVisitor.visitJumpInsn(GOTO, labels.get(node.getRightNode().getNodeIndex()));
    }

    private void loadFeatureByIndex(MethodVisitor methodVisitor, int index) {
        // load features[index] to stack
        methodVisitor.visitVarInsn(ALOAD, FEATURE_PARAMETER_INDEX);
        methodVisitor.visitLdcInsn(index);
        methodVisitor.visitInsn(DALOAD);
    }

    private MethodVisitor simpleVisitMethod(ClassVisitor cv, int access, final String name,
                                            final String descriptor) {
        return cv.visitMethod(access, name, descriptor, null, null);
    }

    private String toInternalName(final String name) {
        return StringUtils.join(name.split("\\."), "/");
    }

    private String getSuperName(final TreeModel model) {
        if (model.isContainsCatNode()) {
            return META_DATA_HOLDER_INTERNAL_NAME;
        } else {
            return OBJECT_INTERNAL_NAME;
        }
    }
}
