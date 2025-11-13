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
         *   2: algorithm choice       ("cha" or "rta")
         */
        if (args.length < 3) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.TestCallGraphGenerator <classPath> <pathToTestClasses> <cha|rta>");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.TestCallGraphGenerator ./target/classes ./target/test-classes cha");
            System.exit(1);
        }

        String classPath = args[0];
        String pathToTestClasses = args[1];
        String algorithmChoice = args[2].toLowerCase();

        try {
            CallGraph cg = buildTestCallGraph(classPath, pathToTestClasses, algorithmChoice);
            writeOutputs(cg);
        } catch (IOException e) {
            System.err.println("❌ Test call graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Builds a call graph for all test classes.
     *
     * @param classPath          Path to compiled main classes
     * @param pathToTestClasses  Path to compiled test classes
     * @param algorithmChoice    Call graph construction algorithm ("cha", "rta", etc.)
     * @return The constructed CallGraph
     * @throws IOException If extraction fails
     */
    public static CallGraph buildTestCallGraph(String classPath,
                                               String pathToTestClasses,
                                               String algorithmChoice) throws IOException {
        ExtractCallGraph extractor = new ExtractCallGraph();
        CallGraphConstructionAlgorithm cgAlgorithm = CallGraphConstructionAlgorithm.fromString(algorithmChoice);

        return extractor.extractForTestClasses(classPath, pathToTestClasses, cgAlgorithm);
    }

    /**
     * Writes the test call graph to the output directory.
     *
     * @param cg The generated test CallGraph
     * @throws IOException If writing fails
     */
    public static void writeOutputs(CallGraph cg) throws IOException {
        // Ensure output directory exists
        java.io.File outDir = new java.io.File("out");
        outDir.mkdirs();

        // Write DOT graph representation
        String filename = "out/test_graph_raw.dot";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(
                    cg.exportAsDot().collect(Collectors.joining(System.lineSeparator()))
            );
            System.out.println("✅ Test DOT file written to " + filename);
        }
    }
}
