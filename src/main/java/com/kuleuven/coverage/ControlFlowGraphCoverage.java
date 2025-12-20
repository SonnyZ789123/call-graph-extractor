package com.kuleuven.coverage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.ControlFlowGraph.ExtractControlFlowGraph;
import com.kuleuven.coverage.CoverageAgent.BlockInfo;
import com.kuleuven.coverage.CoverageAgent.JvmDescriptorUtil;
import sootup.core.graph.BasicBlock;
import sootup.core.types.*;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.common.stmt.Stmt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ControlFlowGraphCoverage {
    public static void main(String[] args) {
        /*
         * Expected arguments:
         *   0: classPath              (e.g., "./target/classes")
         *   1: fully-qualified method signature (e.g., "<com.kuleuven._examples.Foo: int foo(int)>")
         */
        if (args.length < 2) {
            System.out.println("Usage: java -cp <jar> com.kuleuven.coverage.ControlFlowGraphCoverage <classPath> <fullyQualifiedMethodSignature>");
            System.out.println("Example: java -cp target/myjar.jar com.kuleuven.coverage.ControlFlowGraphCoverage ./target/classes \"<com.kuleuven._examples.Foo: int foo(int)>\"");
            System.exit(1);
        }

        String classPath = args[0];
        String fullyQualifiedMethodSignature = args[1];

        writeCfgBlockMap(classPath, fullyQualifiedMethodSignature);
    }

    private static Map<Integer, BlockInfo> createCfgBlockMap(String classPath, String fullyQualifiedMethodSignature) {
        ExtractControlFlowGraph extractor = new ExtractControlFlowGraph(
                classPath,
                fullyQualifiedMethodSignature
        );

        MutableBlockStmtGraph cfg = (MutableBlockStmtGraph) extractor.extract();

        Map<Integer, BlockInfo> blocksById = new LinkedHashMap<>();

        int nextId = 0;
        for (BasicBlock<?> block : cfg.getBlocks()) {
            Stmt entry = block.getHead();

            int lineNumber = entry.getPositionInfo().getStmtPosition().getFirstLine();

            BlockInfo info = new BlockInfo(
                    extractor.method.getDeclaringClassType().getFullyQualifiedName(),
                    extractor.method.getName(),
                    JvmDescriptorUtil.toJvmMethodDescriptor(extractor.method),
                    entry.toString(),
                    lineNumber
            );

            blocksById.put(nextId++, info);
        }

        return blocksById;
    }

    public static void writeCfgBlockMap(String classPath, String fullyQualifiedMethodSignature) {
        Map<Integer, BlockInfo> blocksById = createCfgBlockMap(classPath, fullyQualifiedMethodSignature);

        Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        String filename = "out/cfg_block_mapping.json";
        try (FileWriter writer = new FileWriter(filename)) {
            gson.toJson(blocksById, writer);
            System.out.println("✅ CFG block mapping written to " + filename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
