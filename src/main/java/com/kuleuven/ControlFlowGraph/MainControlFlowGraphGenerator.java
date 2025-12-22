package com.kuleuven.ControlFlowGraph;

import sootup.core.graph.ControlFlowGraph;
import sootup.core.util.DotExporter;

import java.io.FileWriter;
import java.io.IOException;

public class MainControlFlowGraphGenerator {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
         */
        if (args.length < 2) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.ControlFlowGraph.MainControlFlowGraphGenerator <classPath> <fullyQualifiedMethodSignature> ");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.ControlFlowGraph.MainControlFlowGraphGenerator ./target/classes \"<com.kuleuven.library.Main: void main(java.lang.String[])>\"");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];

        try {
            ControlFlowGraph<?> cfg = buildControlFlowGraph(classPath, fullyQualifiedMethodSignature);
            writeOutputs(cfg);
        } catch (IOException e) {
            System.err.println("❌ Control flow graph generation failed: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Builds a control flow graph from the given parameters.
     *
     * @param classPath The classpath to analyze (compiled .class files or jars)
     * @param fullyQualifiedMethodSignature Fully-qualified method signature (e.g., "<com.kuleuven.library.Main: void main(java.lang.String[])>")
     * @return The constructed control flow graph
     * @throws IOException If extraction fails
     */
    private static ControlFlowGraph<?> buildControlFlowGraph(String classPath, String fullyQualifiedMethodSignature) throws IOException {
        ExtractControlFlowGraph extractor = new ExtractControlFlowGraph(classPath, fullyQualifiedMethodSignature);

        return extractor.extract();
    }

    /**
     * Writes the control flow graph's DOT representation.
     *
     * @param cfg The control flow graph to write
     * @throws IOException If writing fails
     */
    private static void writeOutputs(ControlFlowGraph<?> cfg) throws IOException {
        // Ensure output directory exists
        (new java.io.File("out")).mkdirs();

        // Write DOT graph representation
        String filename = "out/control_flow_graph_raw.dot";
        try (FileWriter writer = new FileWriter(filename)) {
            String cfgAsDot = DotExporter.buildGraph(cfg, false, null, null);
            writer.write(cfgAsDot);
            System.out.println("✅ DOT file written to " + filename);
        }
    }
}
