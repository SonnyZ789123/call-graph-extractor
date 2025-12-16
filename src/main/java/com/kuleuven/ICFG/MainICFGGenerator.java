package com.kuleuven.ICFG;

import com.kuleuven.metrics.CallGraphConstructionAlgorithm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainICFGGenerator {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         *   2: algorithm choice       ("cha" or "rta")
         *   3: project prefixes       (optional, comma-separated, e.g., "com.kuleuven,org.example")
         */
        if (args.length < 3) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.CallGraph.MainCallGraphGenerator <fullyQualifiedMethodSignature> \"<entryMethodSignature>\" <cha|rta> [projectPrefixes]");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.CallGraph.MainCallGraphGenerator ./target/classes \"<com.kuleuven.library.Main: void main(java.lang.String[])>\" cha com.kuleuven,org.example");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];
        String algorithmChoice = args[2].toLowerCase();
        List<String> projectPrefixes = new ArrayList<>();
        if (args.length >= 4) {
            String[] prefixes = args[3].split(",");
            for (String prefix : prefixes) {
                projectPrefixes.add(prefix.trim());
            }
        }

        try {
            ICFG icfg = buildCallGraph(classPath, fullyQualifiedMethodSignature, algorithmChoice);
        } catch (IOException e) {
            System.err.println("❌ ICFG generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Builds a call graph from the given parameters.
     *
     * @param classPath The classpath to analyze (compiled .class files or jars)
     * @param fullyQualifiedMethodSignature Fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
     * @param algorithmChoice Call graph construction algorithm ("cha", "rta", etc.)
     * @return The constructed ICFG
     * @throws IOException If extraction fails
     */
    public static ICFG buildCallGraph(String classPath,
                                           String fullyQualifiedMethodSignature,
                                           String algorithmChoice) throws IOException {
        ExtractICFG extractor = new ExtractICFG();
        CallGraphConstructionAlgorithm cgAlgorithm = CallGraphConstructionAlgorithm.fromString(algorithmChoice);

        return extractor.extract(classPath, fullyQualifiedMethodSignature, cgAlgorithm);
    }
}
