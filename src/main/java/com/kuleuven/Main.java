package com.kuleuven;

import sootup.callgraph.CallGraphAlgorithm;
import sootup.callgraph.ClassHierarchyAnalysisAlgorithm;
import sootup.callgraph.RapidTypeAnalysisAlgorithm;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        if (args.length < 4 || (!Objects.equals(args[2], "cha") && !Objects.equals(args[2], "rta"))) {
            System.out.println("Need arguments: <classPath> <mainClass> <algorithm> <outputFile>");
            System.out.println("Example: ./target/classes com.kuleuven.library.Main (cha|rta) graph.dot");
            return;
        }

        String classPath = args[0];
        String mainClassName = args[1];
        String algorithmChoice = args[2].toLowerCase();
        String outputFile = args[3];

        AnalysisInputLocation inputLocation =
                new JavaClassPathAnalysisInputLocation(classPath);

        JavaView view = new JavaView(inputLocation);

        JavaClassType classType =
                view.getIdentifierFactory().getClassType(mainClassName);

        Optional<JavaSootClass> maybeClass = view.getClass(classType);
        if (maybeClass.isEmpty()) {
            System.err.println("Error: Could not load class " + mainClassName);
            return;
        }

        JavaSootClass sootClass = maybeClass.get();

        MethodSignature methodSignature = view.getIdentifierFactory().getMethodSignature(
                classType,
                "main",
                "void",
                Collections.singletonList("java.lang.String[]")
        );

        MethodSubSignature mss = methodSignature.getSubSignature();
        Optional<JavaSootMethod> opt = sootClass.getMethod(mss);

        if (opt.isEmpty()) {
            System.err.println("Error: Could not find main(String[]) in " + mainClassName);
            return;
        }

        CallGraphAlgorithm cgAlgorithm;
        switch (algorithmChoice) {
            case "cha":
                cgAlgorithm = new ClassHierarchyAnalysisAlgorithm(view);
                break;
            case "rta":
                cgAlgorithm = new RapidTypeAnalysisAlgorithm(view);
                break;
            default:
                return;
        }

        sootup.callgraph.CallGraph cg =
                cgAlgorithm.initialize(Collections.singletonList(methodSignature));

        try (FileWriter writer = new FileWriter("out/" + outputFile)) {
            writer.write(cg.exportAsDot());
            System.out.println("✅ DOT file written to out/" + outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
