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
         *   1: fully-qualified class  (e.g., "com.kuleuven.library.Main")
         *   2: entry method signature (e.g., "void main(java.lang.String[])")
         *   3: path to test classes   (e.g., "./target/test-classes")
         *   3: algorithm choice       ("cha" or "rta")
         *   4: project prefixes       (optional, comma-separated, e.g., "com.kuleuven,org.example")
         */
        if (args.length < 4) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.CallGraph.MainCallGraphGenerator <classPath> <mainClass> \"<entryMethodSignature>\" <pathToTestClasses> <cha|rta> <projectPrefixes>");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.CallGraph.MainCallGraphGenerator ./target/classes com.kuleuven.library.Main \"void main(java.lang.String[])\" ./target/test-classes cha com.kuleuven,org.example");
            System.exit(1);
        }

        String classPath = args[0];
        String mainClassName = args[1];
        String entryMethodSignature = args[2];
        String pathToTestClasses = args[3];
        String algorithmChoice = args[4].toLowerCase();
        List<String> projectPrefixes = new ArrayList<>();
        if (args.length >= 6) {
            String[] prefixes = args[5].split(",");
            for (String prefix : prefixes) {
                projectPrefixes.add(prefix.trim());
            }
        }

        try {
            CallGraph cg = MainCallGraphGenerator.buildCallGraph(classPath, mainClassName, entryMethodSignature, algorithmChoice);
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
