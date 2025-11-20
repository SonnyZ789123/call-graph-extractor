package com.kuleuven.CallGraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.coverage.CallGraphCoverage;
import sootup.callgraph.CallGraph;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CallGraphWithCoverageGenerator {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: path to test classes   (e.g., "./target/test-classes")
         *   2: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         *   3: algorithm choice       ("cha" or "rta")
         *   4: project prefixes       (optional, comma-separated, e.g., "com.kuleuven,org.example")
         */
        if (args.length < 4) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.CallGraph.MainCallGraphGenerator <classPath> <pathToTestClasses> <fullyQualifiedMethodSignature> <cha|rta> [projectPrefixes]");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.CallGraph.MainCallGraphGenerator ./target/classes ./target/test-classes \"<com.kuleuven.library.Main: void main(java.lang.String[])>\" cha com.kuleuven,org.example");
            System.exit(1);
        }

        String classPath = args[0];
        String pathToTestClasses = args[1];
        String fullyQualifiedMethodSignature = args[2];
        String algorithmChoice = args[3].toLowerCase();
        List<String> projectPrefixes = new ArrayList<>();
        if (args.length >= 5) {
            String[] prefixes = args[4].split(",");
            for (String prefix : prefixes) {
                projectPrefixes.add(prefix.trim());
            }
        }

        try {
            CallGraph cg = MainCallGraphGenerator.buildCallGraph(classPath, fullyQualifiedMethodSignature, algorithmChoice);
            MainCallGraphGenerator.writeOutputs(cg, projectPrefixes);

            CallGraph testCg = TestCallGraphGenerator.buildTestCallGraph(classPath, pathToTestClasses, algorithmChoice);
            TestCallGraphGenerator.writeOutputs(testCg, projectPrefixes);

            CallGraphCoverage coverage = new CallGraphCoverage();
            CallGraphCoverage.CoverageResult coverageResult = coverage.calculateCoverage(cg, testCg);

            writeOutputs(coverageResult);
        } catch (IOException e) {
            System.err.println("❌ Call graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Writes the node and edge coverage to the output directory.
     *
     * @param coverage The coverage result
     * @throws IOException If writing fails
     */
    public static void writeOutputs(CallGraphCoverage.CoverageResult coverage) throws IOException {
        // Ensure output directory exists
        new java.io.File("out").mkdirs();

        // Write the node and edge coverage to a json file
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter("out/node_coverage.json")) {
            gson.toJson(coverage.getNodeCoverage(), writer);
        }

        try (FileWriter writer = new FileWriter("out/edge_coverage.json")) {
            gson.toJson(coverage.getEdgeCoverage(), writer);
        }

        System.out.println("✅ Coverage data written to:");
        System.out.println("   out/node_coverage.json");
        System.out.println("   out/edge_coverage.json");
    }
}
