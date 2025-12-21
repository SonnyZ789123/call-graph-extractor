package com.kuleuven.coverage.ControlFlowGraph.Graph;

import com.kuleuven.coverage.CoverageAgent.BlockInfo;
import org.checkerframework.checker.nullness.qual.NonNull;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;
import sootup.core.graph.BasicBlock;

import java.util.Objects;

public class CoverageNode extends PropertyGraphNode {
    private final BlockInfo blockInfo;
    private final BasicBlock<?> block;

    public CoverageNode(@NonNull BlockInfo blockInfo, @NonNull BasicBlock<?> block) {
        this.blockInfo = blockInfo;
        this.block = block;
    }

    public BlockInfo getBlockInfo() {
        return blockInfo;
    }

    public BasicBlock<?> getBlock() {
        return block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoverageNode that = (CoverageNode) o;
        return blockInfo.equals(that.getBlockInfo());
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockInfo, block);
    }

    @Override
    public String toString() {
        return blockInfo.className() + "." +
                blockInfo.methodName() + blockInfo.methodDescriptor() + " " +
                blockInfo.stmtId();
    }
}
