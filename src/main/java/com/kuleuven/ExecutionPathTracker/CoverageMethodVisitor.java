//package com.kuleuven.ExecutionPathTracker;
//
//import org.objectweb.asm.Label;
//import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;
//
//class CoverageMethodVisitor extends MethodVisitor {
//
//    private final String className;
//    private final String methodName;
//    private final String desc;
//    private int currentOffset = 0;
//
//    CoverageMethodVisitor(MethodVisitor mv, String cls, String m, String d) {
//        super(Opcodes.ASM9, mv);
//        this.className = cls;
//        this.methodName = m;
//        this.desc = d;
//    }
//
//    @Override
//    public void visitLabel(Label label) {
//        Integer blockId = lookupBlockId(className, methodName, desc, currentOffset);
//        if (blockId != null) {
//            injectHit(blockId);
//        }
//        super.visitLabel(label);
//    }
//
//    private void injectHit(int blockId) {
//        mv.visitLdcInsn(blockId);
//        mv.visitMethodInsn(
//                Opcodes.INVOKESTATIC,
//                "coverage/CoverageRuntime",
//                "hit",
//                "(I)V",
//                false
//        );
//    }
//
//    @Override
//    public void visitInsn(int opcode) {
//        currentOffset++;
//        super.visitInsn(opcode);
//    }
//}
//
