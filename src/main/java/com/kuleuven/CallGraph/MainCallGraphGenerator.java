package com.kuleuven.CallGraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.metrics.CallGraphConstructionAlgorithm;
import com.kuleuven.metrics.PageRank;
import org.jspecify.annotations.Nullable;
import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MainCallGraphGenerator {

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
            CallGraph cg = buildCallGraph(classPath, fullyQualifiedMethodSignature, algorithmChoice);
            writeOutputs(cg, projectPrefixes);
        } catch (IOException e) {
            System.err.println("❌ Call graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Builds a call graph from the given parameters.
     *
     * @param classPath The classpath to analyze (compiled .class files or jars)
     * @param fullyQualifiedMethodSignature Fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
     * @param algorithmChoice Call graph construction algorithm ("cha", "rta", etc.)
     * @return The constructed CallGraph
     * @throws IOException If extraction fails
     */
    public static CallGraph buildCallGraph(String classPath,
                                           String fullyQualifiedMethodSignature,
                                           String algorithmChoice) throws IOException {
        ExtractCallGraph extractor = new ExtractCallGraph();
        CallGraphConstructionAlgorithm cgAlgorithm = CallGraphConstructionAlgorithm.fromString(algorithmChoice);

        return extractor.extract(classPath, fullyQualifiedMethodSignature, cgAlgorithm);
    }

    /**
     * Writes the call graph and its PageRank scores to the output directory.
     *
     * @param cg The generated CallGraph
     * @param projectPrefixes List of project package prefixes to filter methods
     * @throws IOException If writing fails
     */
    public static void writeOutputs(CallGraph cg, @Nullable List<String> projectPrefixes) throws IOException {
        // Ensure output directory exists
        java.io.File outDir = new java.io.File("out");
        outDir.mkdirs();

        // Write DOT graph representation
        String filename = "out/graph_raw.dot";
        try (FileWriter writer = new FileWriter(filename)) {
            ProjectMethodFilter filter = new ProjectMethodFilter(projectPrefixes);
            writer.write(
                    filter.filterMethodsFromDotFile(cg.exportAsDot())
                            .collect(Collectors.joining(System.lineSeparator()))
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
