package com.kuleuven.CallGraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
            System.out.println("Usage: java -cp <jar> com.kuleuven.CallGraph.MainCallGraphGenerator <classPath> <mainClass> \"<entryMethodSignature>\" <cha|rta>");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.CallGraph.MainCallGraphGenerator ./target/classes com.kuleuven.library.Main \"void main(java.lang.String[])\" cha");
            System.exit(1);
        }

        String classPath = args[0];
        String mainClassName = args[1];
        String entryMethodSignature = args[2];
        String algorithmChoice = args[3].toLowerCase();

        try {
            CallGraph cg = buildCallGraph(classPath, mainClassName, entryMethodSignature, algorithmChoice);
            writeOutputs(cg);
        } catch (IOException e) {
            System.err.println("❌ Call graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Builds a call graph from the given parameters.
     *
     * @param classPath The classpath to analyze (compiled .class files or jars)
     * @param mainClassName Fully-qualified class name containing the entry method
     * @param entryMethodSignature Signature of the entry method (e.g. "void main(java.lang.String[])")
     * @param algorithmChoice Call graph construction algorithm ("cha", "rta", etc.)
     * @return The constructed CallGraph
     * @throws IOException If extraction fails
     */
    public static CallGraph buildCallGraph(String classPath,
                                           String mainClassName,
                                           String entryMethodSignature,
                                           String algorithmChoice) throws IOException {
        ExtractCallGraph extractor = new ExtractCallGraph();
        CallGraphConstructionAlgorithm cgAlgorithm = CallGraphConstructionAlgorithm.fromString(algorithmChoice);

        return extractor.extract(classPath, mainClassName, entryMethodSignature, cgAlgorithm);
    }

    /**
     * Writes the call graph and its PageRank scores to the output directory.
     *
     * @param cg The generated CallGraph
     * @throws IOException If writing fails
     */
    public static void writeOutputs(CallGraph cg) throws IOException {
        // Ensure output directory exists
        java.io.File outDir = new java.io.File("out");
        outDir.mkdirs();

        // Write DOT graph representation
        String filename = "out/graph_raw.dot";
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(
                    cg.exportAsDot().collect(Collectors.joining(System.lineSeparator()))
            );
            System.out.println("✅ DOT file written to " + filename);
        }

        // Compute PageRank
        PageRank pageRank = new PageRank();
        SootUpCallGraphWrapper cgWrapper = new SootUpCallGraphWrapper(cg);
        Map<MethodSignature, Double> pageRankScores = pageRank.computePageRank(cgWrapper);
        Map<String, Double> stringKeyedScores = pageRankScores.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().toString(),
                        Map.Entry::getValue
                ));

        // Write graph ranking
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try (FileWriter writer = new FileWriter("out/graph_ranking.json")) {
            gson.toJson(stringKeyedScores, writer);
            System.out.println("✅ Graph ranking written to out/graph_ranking.json");
        }
    }
}
