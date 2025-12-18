//package com.kuleuven.ExecutionPathTracker;
//
//import org.objectweb.asm.ClassReader;
//import org.objectweb.asm.ClassVisitor;
//import org.objectweb.asm.ClassWriter;
//
//import java.lang.instrument.Instrumentation;
//
//public class CoverageAgent {
//    public static void premain(String args, Instrumentation inst) {
//        inst.addTransformer((loader, name, cls, domain, bytes) -> {
//            if (!shouldInstrument(name)) return bytes;
//
//            ClassReader cr = new ClassReader(bytes);
//            ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);
//
//            ClassVisitor cv = new CoverageClassVisitor(cw);
//            cr.accept(cv, 0);
//
//            return cw.toByteArray();
//        });
//    }
//}
