package com.kuleuven.coverage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kuleuven.ControlFlowGraph.ExtractControlFlowGraph;
import com.kuleuven.coverage.CoverageAgent.BlockInfo;
import sootup.core.graph.BasicBlock;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.jimple.common.stmt.Stmt;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class ControlFlowGraphCoverage {
    public static void main(String[] args) {
        ExtractControlFlowGraph extractor = new ExtractControlFlowGraph();

        MutableBlockStmtGraph cfg = (MutableBlockStmtGraph) extractor.extract(
                "./target/classes",
                "<com.kuleuven._examples.Foo: int foo(int)>"
        );

        Map<Integer, BlockInfo> blocksById = new LinkedHashMap<>();

        int nextId = 0;
        for (BasicBlock<?> block : cfg.getBlocks()) {
            Stmt entry = block.getHead();

            int lineNumber = entry.getPositionInfo().getStmtPosition().getFirstLine();

            BlockInfo info = new BlockInfo(
                    "com.kuleuven._examples.Foo",
                    "foo",
                    "(I)I",
                    entry.toString(),
                    lineNumber
            );

            blocksById.put(nextId++, info);
        }

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
