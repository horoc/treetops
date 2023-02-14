package org.treetops.core.generator;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.CheckClassAdapter;
import org.treetops.core.model.MissingType;
import org.treetops.core.model.TreeModel;
import org.treetops.core.model.TreeNode;

/**
 * @author chenzhou@apache.org
 * @date 2023/2/14
 */
public class AsmGenerator extends ClassLoader implements Generator, Opcodes {

    private static final int featureParameterIndex = 1;

    private AsmGenerator() {
    }

    public static AsmGenerator getInstance() {
        /*
            It's not singleton here, will create a new class loader everytime
            aim to help gc unload unused model
         */
        return new AsmGenerator();
    }

    @Override
    public Class<?> defineClassFromCode(String name, byte[] code) {
        return this.defineClass(name, code, 0, code.length);
    }

    @Override
    public byte[] generateCode(String name, TreeModel model) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = new CheckClassAdapter(cw);
        String className = toInternalName(name);

        // define class
        cv.visit(V1_8, ACC_PUBLIC | ACC_SUPER, toInternalName(name), null, getSuperName(model),
            new String[] {"org/treetops/core/predictor/Predictor"});

        // define method
        addInitMethod(cv, className, model);

        // tree decision method
        // description : private double tree_[%tree_index](double[] features);
        model.getTrees().forEach(t -> addTreeMethod(cv, className, t));

        // prediction method
        addPredictionMethod(cv, className, model);

        cv.visitEnd();
        return cw.toByteArray();
    }

    private void addInitMethod(ClassVisitor cv, String className, TreeModel model) {
        MethodVisitor methodVisitor = cv.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        methodVisitor.visitCode();
        methodVisitor.visitVarInsn(ALOAD, 0);
        if (model.isContainsCatNode()) {
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "org/treetops/core/predictor/MetaDataHolder", "<init>", "()V", false);
        } else {
            methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        }
        methodVisitor.visitInsn(RETURN);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    private void addPredictionMethod(ClassVisitor cv, String className, TreeModel model) {
        MethodVisitor methodVisitor = cv.visitMethod(ACC_PUBLIC, "predictRaw", "([D)[D", null, null);
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
            methodVisitor.visitMethodInsn(INVOKESPECIAL, className, "tree_" + root.getTreeIndex(), "([D)D", false);
            methodVisitor.visitInsn(DADD);
            methodVisitor.visitInsn(DASTORE);
        }

        methodVisitor.visitVarInsn(ALOAD, 2);
        methodVisitor.visitInsn(ARETURN);
        methodVisitor.visitMaxs(1, 1);
        methodVisitor.visitEnd();
    }

    private void addTreeMethod(ClassVisitor classWriter, String className, TreeNode root) {
        {
            MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PRIVATE, "tree_" + root.getTreeIndex(), "([D)D", null, null);
            methodVisitor.visitCode();

            Map<Integer, Label> labels = new HashMap<>();
            for (TreeNode node : root.getAllNodes()) {
                labels.put(node.getNodeIndex(), new Label());
            }

            for (TreeNode node : root.getAllNodes()) {
                defineNodeBlock(node, className, methodVisitor, labels);
            }

            methodVisitor.visitMaxs(1, 1);
            methodVisitor.visitEnd();
        }
    }

    private void defineNodeBlock(TreeNode node, String className, MethodVisitor methodVisitor, Map<Integer, Label> labels) {
        if (node.isLeaf()) {
            defineLeafNodeBlock(node, methodVisitor, labels);
            return;
        }

        if (node.isCategoryNode()) {
            defineCategoryNodeBlock(node, className, methodVisitor, labels);
        } else {
            defineNumericalNodeBlock(node, methodVisitor, labels);
        }
    }

    private void defineLeafNodeBlock(TreeNode node, MethodVisitor methodVisitor, Map<Integer, Label> labels) {
        int nodeIndex = node.getNodeIndex();
        methodVisitor.visitLabel(labels.get(nodeIndex));
        methodVisitor.visitLdcInsn(new Double(node.getLeafValue()));
        methodVisitor.visitInsn(DRETURN);
    }

    @SuppressWarnings("Duplicates")
    private void defineNumericalNodeBlock(TreeNode node, MethodVisitor methodVisitor, Map<Integer, Label> labels) {
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
            methodVisitor.visitLdcInsn(new Double("-1E-35"));
            methodVisitor.visitInsn(DCMPL);
            methodVisitor.visitJumpInsn(IFLT, label);

            // if feature > 1e-35, not zero, jump to continue
            methodVisitor.visitVarInsn(DLOAD, 2);
            methodVisitor.visitLdcInsn(new Double("1E-35"));
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

    private void defineCategoryNodeBlock(TreeNode node, String className, MethodVisitor methodVisitor, Map<Integer, Label> labels) {
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
        methodVisitor.visitMethodInsn(INVOKEVIRTUAL, className, "findCatBitset", "(IIID)Z", false);
        methodVisitor.visitJumpInsn(IFNE, labels.get(node.getLeftNode().getNodeIndex()));

        // others, jump left child node
        methodVisitor.visitJumpInsn(GOTO, labels.get(node.getRightNode().getNodeIndex()));
    }

    private void loadFeatureByIndex(MethodVisitor methodVisitor, int index) {
        // load features[index] to stack
        methodVisitor.visitVarInsn(ALOAD, featureParameterIndex);
        methodVisitor.visitLdcInsn(index);
        methodVisitor.visitInsn(DALOAD);
    }


    private String toInternalName(String name) {
        return StringUtils.join(name.split("\\."), "/");
    }

    private String getSuperName(TreeModel model) {
        if (model.isContainsCatNode()) {
            return "org/treetops/core/predictor/MetaDataHolder";
        } else {
            return "java/lang/Object";
        }
    }


    // todo method too large
    private void initCatBitSet(MethodVisitor methodVisitor, String className, TreeModel treeModel) {
        methodVisitor.visitIntInsn(SIPUSH, treeModel.getTrees().size());
        methodVisitor.visitTypeInsn(ANEWARRAY, "[J");
        methodVisitor.visitInsn(DUP);
        for (int i = 0; i < treeModel.getTrees().size(); i++) {
            TreeNode node = treeModel.getTrees().get(i);
            int catThresholdSize = Optional.ofNullable(node.getCatThreshold()).map(List::size).orElse(0);
            methodVisitor.visitIntInsn(SIPUSH, i);
            methodVisitor.visitIntInsn(SIPUSH, catThresholdSize);
            methodVisitor.visitIntInsn(NEWARRAY, T_LONG);
            methodVisitor.visitInsn(DUP);

            for (int j = 0; j < catThresholdSize; j++) {
                methodVisitor.visitIntInsn(SIPUSH, j);
                methodVisitor.visitLdcInsn(new Long(node.getCatThreshold().get(j)));
                methodVisitor.visitInsn(LASTORE);
                if (j == node.getCatThreshold().size() - 1) {
                    methodVisitor.visitInsn(AASTORE);
                } else {
                    methodVisitor.visitInsn(DUP);
                }
            }
            if (i != treeModel.getTrees().size() - 1) {
                methodVisitor.visitInsn(DUP);
            }
        }
        methodVisitor.visitFieldInsn(PUTSTATIC, className, "catBitSet", "[[J");
    }
}
