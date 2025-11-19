package com.kuleuven.CallGraph;

import com.kuleuven.metrics.CallGraphConstructionAlgorithm;
import org.checkerframework.checker.units.qual.A;
import org.jspecify.annotations.Nullable;
import sootup.callgraph.CallGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestCallGraphGenerator {

    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: pathToTestClasses      (e.g., "./target/test-classes")
         *   2: algorithm choice       ("cha" or "rta")
         *   3: project prefixes       (optional, comma-separated, e.g., "com.kuleuven,org.example")
         */
        if (args.length < 3) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.CallGraph.TestCallGraphGenerator <classPath> <pathToTestClasses> <cha|rta> <projectPrefixes>");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.CallGraph.TestCallGraphGenerator ./target/classes ./target/test-classes cha com.kuleuven,org.example");
            System.exit(1);
        }

        String classPath = args[0];
        String pathToTestClasses = args[1];
        String algorithmChoice = args[2].toLowerCase();
        List<String> projectPrefixes = new ArrayList<>();
        if (args.length >= 4) {
            String[] prefixes = args[3].split(",");
            for (String prefix : prefixes) {
                projectPrefixes.add(prefix.trim());
            }
        }

        try {
            CallGraph cg = buildTestCallGraph(classPath, pathToTestClasses, algorithmChoice);
            writeOutputs(cg, projectPrefixes);
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
     * @param projectPrefixes List of project package prefixes for filtering
     * @throws IOException If writing fails
     */
    public static void writeOutputs(CallGraph cg, @Nullable List<String> projectPrefixes) throws IOException {
        // Ensure output directory exists
        java.io.File outDir = new java.io.File("out");
        outDir.mkdirs();

        // Write DOT graph representation
        String filename = "out/test_graph_raw.dot";
        try (FileWriter writer = new FileWriter(filename)) {
            ProjectMethodFilter filter = new ProjectMethodFilter(projectPrefixes);
            writer.write(
                    filter.filterMethodsFromDotFile(cg.exportAsDot())
                            .collect(Collectors.joining(System.lineSeparator()))
            );
            System.out.println("✅ Test DOT file written to " + filename);
        }
    }
}
