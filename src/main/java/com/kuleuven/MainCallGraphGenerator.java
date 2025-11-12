package com.kuleuven;

import com.kuleuven.metrics.CallGraphConstructionAlgorithm;
import com.kuleuven.metrics.PageRank;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class MainCallGraphGenerator {

    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified class  (e.g., "com.kuleuven.library.Main")
         *   2: entry method signature (e.g., "void main(java.lang.String[])")
         *   3: algorithm choice       ("cha" or "rta")
         */
        if (args.length < 4) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.MainCallGraphGenerator <classPath> <mainClass> \"<entryMethodSignature>\" <cha|rta>");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.MainCallGraphGenerator ./target/classes com.kuleuven.library.Main \"void main(java.lang.String[])\" cha");
            System.exit(1);
        }

        String classPath = args[0];
        String mainClassName = args[1];
        String entryMethodSignature = args[2]; // Example: "void main(java.lang.String[])"
        String algorithmChoice = args[3].toLowerCase();

        try {
            ExtractCallGraph extractor = new ExtractCallGraph();

            CallGraphConstructionAlgorithm cgAlgorithm = CallGraphConstructionAlgorithm.fromString(algorithmChoice);

            // Build the call graph starting from the given entry method
            CallGraph cg = extractor.extract(classPath, mainClassName, entryMethodSignature, cgAlgorithm);

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

            // Gives the nodes a score based on PageRank algorithm
            PageRank pageRank = new PageRank();
            Map<MethodSignature, Double> pageRankScores = pageRank.computePageRank(cg);

            // Write PageRank scores to a text file
            String scoreFilename = "out/scores.txt";
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
