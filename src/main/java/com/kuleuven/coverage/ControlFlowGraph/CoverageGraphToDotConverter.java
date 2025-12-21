package com.kuleuven.coverage.ControlFlowGraph;

import java.util.Comparator;

import com.kuleuven.coverage.ControlFlowGraph.Graph.CFGCoverageGraph;
import com.kuleuven.coverage.ControlFlowGraph.Graph.CoverageEdge;
import com.kuleuven.coverage.ControlFlowGraph.Graph.CoverageNode;
import com.kuleuven.coverage.CoverageAgent.BlockInfo;
import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;

/*
Logic referenced of PropertyGraphToDotConverter of SootUp 2.0.
*/
public class CoverageGraphToDotConverter {
    public static String convert(CFGCoverageGraph graph) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("digraph %s {\n", graph.getName()));
        builder.append("\trankdir=TB;\n");
        builder.append("\tnode [style=filled, shape=record];\n");
        builder.append("\tedge [style=filled]\n");

        for (CoverageNode node : graph.getNodes().stream()
                .map(n -> (CoverageNode) n)
                .sorted(Comparator.comparing(CoverageNode::getBlockInfo, Comparator.comparingInt(BlockInfo::blockId)))
                .toList()) {
            int blockId = node.getBlockInfo().blockId();
            String nodeId = String.valueOf(blockId);
            String label = getNodeLabel(node);
            String color = getNodeColor(node);
            builder.append(String.format("\t\"%s\" [label=\"%s\", fillcolor=\"%s\"];\n", nodeId, label, color));
        }

        for (CoverageEdge edge : graph.getEdges().stream()
                .map(e -> (CoverageEdge) e)
                .sorted(Comparator.comparing((CoverageEdge e) -> e.getSource().getBlockInfo().blockId())
                        .thenComparing((e) -> e.getDestination().getBlockInfo().blockId())
                        .thenComparing(CoverageEdge::getLabel)).toList()) {
            String sourceId = String.valueOf(edge.getSource().getBlockInfo().blockId());
            String destinationId = String.valueOf(edge.getDestination().getBlockInfo().blockId());
            String label = escapeDot(edge.getLabel());
            String color = getEdgeColor(edge);
            builder.append(String.format("\t\"%s\" -> \"%s\"[label=\"%s\", color=\"%s\", fontcolor=\"%s\"];\n", sourceId, destinationId, label, color, color));
        }

        builder.append("}\n");
        return builder.toString();
    }

    private static String escapeDot(String label) {
        return label.replace("\"", "\\\"").replace("<", "&lt;").replace(">", "&gt;").replace("{", "\\{").replace("}", "\\}");
    }

    private static String getNodeLabel(CoverageNode node) {
        return escapeDot(node.toString());
    }

    private static String getNodeColor(CoverageNode node) {
        return "lightblue";
    }

    private static String getEdgeColor(PropertyGraphEdge edge) {
        return "black";
    }
}
