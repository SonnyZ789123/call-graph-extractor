package com.kuleuven.coverage.ExecutionPathTracker;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

class CoverageMethodVisitor extends MethodVisitor {

    private final String className;
    private final String methodName;
    private final String desc;
    private int currentOffset = 0;

    CoverageMethodVisitor(MethodVisitor mv, String cls, String m, String d) {
        super(Opcodes.ASM9, mv);
        this.className = cls;
        this.methodName = m;
        this.desc = d;
    }

    @Override
    public void visitLabel(Label label) {
        System.out.println("================ visitLabel ================");
        System.out.printf("start: %s\n", label.toString());

//        Integer blockId = lookupBlockId(className, methodName, desc, currentOffset);
//        if (blockId != null) {
//            injectHit(blockId);
//        }
        super.visitLabel(label);
    }

    private Integer lookupBlockId(
            String className,
            String methodName,
            String methodDesc,
            int bytecodeOffset
    ) {
        String asmMethodDescriptor = methodName + methodDesc;

        for (Map.Entry<Integer, BlockInfo> e : BlockRegistry.getBlocks().entrySet()) {
            BlockInfo info = e.getValue();

            if (!info.className().equals(className)) {
                continue;
            }

            if (!info.methodDescriptor().equals(asmMethodDescriptor)) {
                continue;
            }

            if (info.bytecodeOffset() == bytecodeOffset) {
                return e.getKey();
            }
        }

        return null;
    }


    private void injectHit(int blockId) {
        mv.visitLdcInsn(blockId);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "coverage/CoverageRuntime",
                "hit",
                "(I)V",
                false
        );
    }

    @Override
    public void visitLineNumber(int line, Label start) {
//        Integer blockId = lookupBlockIdByLine(className, methodName, desc, line);
        System.out.println("================ visitLineNumber ================");
        System.out.printf("line: %s\n", line);
        System.out.printf("start: %s\n", start.toString());
        Integer blockId = null;
        if (blockId != null) {
            injectHit(blockId);
        }
        super.visitLineNumber(line, start);
    }
}

