package com.kuleuven;

import sootup.callgraph.CallGraph;
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
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {

        System.out.println(Arrays.stream(args).toList());

        // ✅ Require 4 args: classPath, className, algorithm, entry method
        if (args.length < 4) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.Main <classPath> <mainClass> \"<entryMethodSignature>\" <cha|rta>");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.Main ./target/classes com.kuleuven.library.Main \"void main(java.lang.String[])\"");
            System.exit(1);;
        }

        String classPath = args[0];
        String mainClassName = args[1];
        String entryMethodSignature = args[2]; // e.g. "void main(java.lang.String[])"
        String algorithmChoice = args[3].toLowerCase();

        if (!algorithmChoice.equals("cha") && !algorithmChoice.equals("rta")) {
            System.err.println("❌ Unknown algorithm: " + algorithmChoice);
            System.exit(1);
        }

        // Parse entrySignature
        String returnType = entryMethodSignature.substring(0, entryMethodSignature.indexOf(' ')).trim();
        String afterReturn = entryMethodSignature.substring(entryMethodSignature.indexOf(' ') + 1).trim();
        String methodName = afterReturn.substring(0, afterReturn.indexOf('(')).trim();
        String paramList = afterReturn.substring(afterReturn.indexOf('(') + 1, afterReturn.indexOf(')')).trim();

        List<String> parameters;
        if (paramList.isEmpty()) {
            parameters = Collections.emptyList();
        } else {
            parameters = Arrays.stream(paramList.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
        }

        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath);
        JavaView view = new JavaView(inputLocation);

        JavaClassType classType = view.getIdentifierFactory().getClassType(mainClassName);
        Optional<JavaSootClass> maybeClass = view.getClass(classType);

        if (maybeClass.isEmpty()) {
            System.err.println("❌ Could not load class " + mainClassName);
            System.exit(1);
        }

        JavaSootClass sootClass = maybeClass.get();

        // ✅ Create entry method signature
        MethodSignature methodSignature = view.getIdentifierFactory().getMethodSignature(
                classType,
                methodName,
                returnType,
                parameters
        );

        MethodSubSignature mss = methodSignature.getSubSignature();
        Optional<JavaSootMethod> opt = sootClass.getMethod(mss);

        if (opt.isEmpty()) {
            System.err.println("❌ Method not found: " + entryMethodSignature);
            System.exit(1);
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
                System.exit(1);
                return;
        }

        try {
            CallGraph cg =
                    cgAlgorithm.initialize(Collections.singletonList(methodSignature));

            new java.io.File("out").mkdirs();

            String filename = "out/graph_raw.dot";
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(cg.exportAsDot());
                System.out.println("✅ DOT file written to " + filename);
            }
        } catch (IOException e) {
            System.err.println("❌ Call graph generation failed.");
            System.exit(1);
        }
    }
}
