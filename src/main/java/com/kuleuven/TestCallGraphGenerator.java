package com.kuleuven;

import com.kuleuven.metrics.CallGraphConstructionAlgorithm;
import sootup.callgraph.CallGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.stream.Collectors;

public class TestCallGraphGenerator {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: pathToTestClasses      (e.g., "./target/test-classes")
         *   1: algorithm choice       ("cha" or "rta")
         */
        if (args.length < 3) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.TestCallGraphGenerator <classPath> <pathToTestClasses> <cha|rta>");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.TestCallGraphGenerator .target/classes .target/test-classes cha");
            System.exit(1);
        }

        String classPath = args[0];
        String pathToTestClasses = args[1];
        String algorithmChoice = args[2].toLowerCase();

        try {
            ExtractCallGraph extractor = new ExtractCallGraph();

            CallGraphConstructionAlgorithm cgAlgorithm = CallGraphConstructionAlgorithm.fromString(algorithmChoice);

            CallGraph cg = extractor.extractForTestClasses(classPath, pathToTestClasses, cgAlgorithm);

            // Ensure output directory exists
            new java.io.File("out").mkdirs();

            // Write DOT graph representation
            String filename = "out/test_graph_raw.dot";
            try (FileWriter writer = new FileWriter(filename)) {
                writer.write(
                        cg.exportAsDot().collect(Collectors.joining(System.lineSeparator()))
                );
                System.out.println("✅ DOT file written to " + filename);
            }
        } catch (IOException e) {
            System.err.println("❌ Call graph generation failed.");
            System.exit(1);
        }
    }
}
