package com.kuleuven.coverage.ControlFlowGraph.Graph;

import com.kuleuven.coverage.CoverageAgent.BlockInfo;
import com.kuleuven.coverage.CoverageAgent.SootStmtGraphUtil;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.*;
import sootup.core.graph.MutableBlockStmtGraph;
import sootup.core.graph.StmtGraph;
import sootup.core.jimple.common.stmt.Stmt;

import java.util.Map;

public class CoverageGraph extends MutableBlockStmtGraph {
    private final StmtGraph<?> cfg;
    private final Map<Integer, Integer> blockIdToCoverageCount;
    private final Map<Integer, BlockInfo> blocksById;
    private final PropertyGraph graph;

    public CoverageGraph(
            StmtGraph<?> cfg,
            Map<Integer, BlockInfo> blocksById,
            Map<Integer, Integer> blockIdToCoverageCount) {
        this.cfg = cfg;
        this.blocksById = blocksById;
        this.blockIdToCoverageCount = blockIdToCoverageCount;
        this.graph = createGraph(cfg);
    }

    public PropertyGraph getGraph() {
        return graph;
    }

    private BlockInfo findBlockInfoByStmt(Stmt stmt) {
        String stmtId = SootStmtGraphUtil.getStmtId(stmt);

        return blocksById.values().stream()
                .filter(blockInfo -> blockInfo.stmtId().equals(stmtId))
                .findFirst().orElse(null);
    }

    /**
     * Reference: See CfgCreator class of SootUp 2.0.
     * Creates the coverage graph for the given Soot method.
     *
     * @param stmtGraph the StmtGraph
     * @return the coverage graph
     */
    public PropertyGraph createGraph(StmtGraph<?> stmtGraph) {
        PropertyGraph.Builder graphBuilder = new CFGCoverageGraph.Builder();
        graphBuilder.setName("cfg_coverage");

        stmtGraph.getBlocks().forEach(
            currBlock -> {
                Stmt entryStmt = currBlock.getHead();
                Stmt tailStmt = currBlock.getTail();

                BlockInfo blockInfo = findBlockInfoByStmt(entryStmt);
                if (blockInfo == null) {
                    return;
                }
                CoverageNode sourceBlockNode = new CoverageNode(blockInfo, currBlock);

                int expectedCount = tailStmt.getExpectedSuccessorCount();
                int successorIndex = 0;

                for (Stmt successor : stmtGraph.getAllSuccessors(tailStmt)) {
                    BlockInfo successorBlockInfo = findBlockInfoByStmt(successor);
                    if (successorBlockInfo == null) {
                        continue;
                    }
                    CoverageNode destinationBlockNode = new CoverageNode(successorBlockInfo, currBlock);
                    CoverageEdge edge = CoverageEdge.of(tailStmt, successorIndex, sourceBlockNode, destinationBlockNode);

                    if (successorIndex >= expectedCount) {
                        edge = CoverageEdge.exceptionalEdge(sourceBlockNode, destinationBlockNode);
                    }

                    graphBuilder.addEdge(edge);
                    successorIndex++;
                }
            });

        return graphBuilder.build();
    }
}
