package com.kuleuven.coverage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kuleuven.ControlFlowGraph.MainControlFlowGraphGenerator;
import com.kuleuven.coverage.ControlFlowGraph.Graph.CoverageGraph;
import com.kuleuven.coverage.CoverageAgent.BlockInfo;
import com.kuleuven.coverage.CoverageAgent.JvmDescriptorParser;
import sootup.core.graph.ControlFlowGraph;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class GenerateCFGCoverageGraph {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: blockMapPath           (e.g., "out/cfg_block_mapping.json")
         *   2: blockCoverageMapPath   (e.g., "out/coverage.out")
         */
        if (args.length < 2) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.coverage.GenerateCFGCoverageGraph <classPath> <blockMapPath> <blockCoverageMapPath>");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.coverage.GenerateCFGCoverageGraph ./target/classes out/cfg_block_mapping.json out/coverage.out");
            System.exit(1);
        }

        String classPath = args[0];
        String blockMapPath = args[1];
        String blockCoverageMapPath = args[2];

        Map<Integer, BlockInfo> blockMap = null;

        try {
            blockMap = readBlockMap(blockMapPath);
        } catch (IOException e) {
            System.err.println("❌ Failed to load block map from path " + blockMapPath);
            System.exit(1);
        }

        try (ObjectInputStream ois =
                     new ObjectInputStream(new FileInputStream(blockCoverageMapPath))) {

            @SuppressWarnings("unchecked")
            List<int[]> executionPaths = (List<int[]>) ois.readObject();

            Map<Integer, Integer> coverageCounts = new HashMap<>();
            for (int[] path : executionPaths) {
                for (int blockId : path) {
                    coverageCounts.merge(blockId, 1, Integer::sum);
                }
            }

            Set<String> fullyQualifiedMethodSignatures = extractMethodSignatures(blockMap.values());

            int i = 0;
            for (String methodSignature : fullyQualifiedMethodSignatures) {
                ControlFlowGraph<?> cfg = buildControlFlowGraph(classPath, methodSignature);

                CoverageGraph coverageGraph = new CoverageGraph(cfg, blockMap, coverageCounts);

                writeOutputs(coverageGraph, i);
                i++;
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to read block coverage map from path " + blockCoverageMapPath);
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.err.println("❌ Failed to deserialize block coverage map: " + e.getMessage());
            System.exit(1);
        }
    }

    private static ControlFlowGraph<?> buildControlFlowGraph(String classPath, String fullyQualifiedMethodSignature) throws IOException {
        return MainControlFlowGraphGenerator.buildControlFlowGraph(classPath, fullyQualifiedMethodSignature);
    }

    private static Set<String> extractMethodSignatures(Collection<BlockInfo> blockInfoSet) {
        Set<String> methodSignatures = new HashSet<>();

        for (BlockInfo blockInfo : blockInfoSet) {
            methodSignatures.add(extractMethodSignature(blockInfo));
        }

        return methodSignatures;
    }

    private static String extractMethodSignature(BlockInfo blockInfo) {
        JvmDescriptorParser.ParsedMethodDescriptor methodDesc = JvmDescriptorParser.parseMethodDescriptor(blockInfo.methodDescriptor());

        return String.format(
                "<%s: %s %s(%s)>",
                blockInfo.className(),
                methodDesc.returnType(),
                blockInfo.methodName(),
                String.join(", ", methodDesc.parameterTypes().stream().map(Object::toString).toList())
        );
    }

    private static Map<Integer, BlockInfo> readBlockMap(String blockMapPath) throws IOException {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<Integer, BlockInfo>>() {}.getType();

        try (InputStreamReader reader =
                     new InputStreamReader(
                             java.nio.file.Files.newInputStream(
                                     java.nio.file.Path.of(blockMapPath)))) {

            return gson.fromJson(reader, type);
        }
    }

    public static void writeOutputs(CoverageGraph coverageGraph, int i) throws IOException {
        // Ensure output directory exists
        (new java.io.File("out")).mkdirs();

        // Write DOT graph representation
        String filename = String.format("out/cfg_coverage%s.dot", i);
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write(coverageGraph.getGraph().toDotGraph());
            System.out.println("✅ DOT file written to " + filename);
        }
    }
}
