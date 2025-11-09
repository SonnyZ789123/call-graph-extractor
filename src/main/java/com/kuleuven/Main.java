package com.kuleuven;

import com.kuleuven.metrics.PageRank;
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
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified class  (e.g., "com.kuleuven.library.Main")
         *   2: entry method signature (e.g., "void main(java.lang.String[])")
         *   3: algorithm choice       ("cha" or "rta")
         */
        if (args.length < 4) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.Main <classPath> <mainClass> \"<entryMethodSignature>\" <cha|rta>");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.Main ./target/classes com.kuleuven.library.Main \"void main(java.lang.String[])\" cha");
            System.exit(1);
        }

        String classPath = args[0];
        String mainClassName = args[1];
        String entryMethodSignature = args[2]; // Example: "void main(java.lang.String[])"
        String algorithmChoice = args[3].toLowerCase();

        // Ensure algorithm is valid
        if (!algorithmChoice.equals("cha") && !algorithmChoice.equals("rta")) {
            System.err.println("❌ Unknown algorithm: " + algorithmChoice);
            System.exit(1);
        }

        /*
         * Parse "void main(java.lang.String[])" into:
         *   returnType = "void"
         *   methodName = "main"
         *   parameters = ["java.lang.String[]"]
         */
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

        // Load classes from the given classpath
        AnalysisInputLocation inputLocation = new JavaClassPathAnalysisInputLocation(classPath);
        JavaView view = new JavaView(inputLocation);

        // Resolve the class we start from
        JavaClassType classType = view.getIdentifierFactory().getClassType(mainClassName);
        Optional<JavaSootClass> maybeClass = view.getClass(classType);

        if (maybeClass.isEmpty()) {
            System.err.println("❌ Could not load class " + mainClassName);
            System.exit(1);
        }

        JavaSootClass sootClass = maybeClass.get();

        /*
         * Create the exact method signature in SootUp form.
         * If this does not match exactly, SootUp cannot find the method.
         */
        MethodSignature methodSignature = view.getIdentifierFactory().getMethodSignature(
                classType,
                methodName,
                returnType,
                parameters
        );

        // Check if the method actually exists in the class
        MethodSubSignature mss = methodSignature.getSubSignature();
        Optional<JavaSootMethod> opt = sootClass.getMethod(mss);

        if (opt.isEmpty()) {
            System.err.println("❌ Method not found: " + entryMethodSignature);
            System.exit(1);
        }

        // Select call graph analysis algorithm
        CallGraphAlgorithm cgAlgorithm = switch (algorithmChoice) {
            case "cha" -> new ClassHierarchyAnalysisAlgorithm(view); // Fast, over-approximates
            case "rta" -> new RapidTypeAnalysisAlgorithm(view);      // More precise, slower
            default -> null;
        };

        try {
            // Build the call graph starting from the given entry method
            CallGraph cg = cgAlgorithm.initialize(Collections.singletonList(methodSignature));

            // Gives the nodes a score based on PageRank algorithm
            PageRank pageRank = new PageRank();
            Map<MethodSignature, Double> pageRankScores = pageRank.computePageRank(cg);

            // Ensure output directory exists
            new java.io.File("out").mkdirs();

            // Write DOT graph representation
            String filename = "out/graph_raw.dot";
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(
                        cg.exportAsDot().collect(Collectors.joining(System.lineSeparator()))
                );
                System.out.println("✅ DOT file written to " + filename);
            }

            // Write PageRank scores to a text file
            String scoreFilename = "out/pagerank_scores.txt";
            try (FileWriter writer = new FileWriter(scoreFilename)) {
                for (Map.Entry<MethodSignature, Double> entry : pageRankScores.entrySet()) {
                    writer.write(entry.getKey().toString() + " | " + entry.getValue() + System.lineSeparator());
                }
                System.out.println("✅ PageRank scores written to " + scoreFilename);
            }

        } catch (IOException e) {
            System.err.println("❌ Call graph generation failed.");
            System.exit(1);
        }
    }
}
