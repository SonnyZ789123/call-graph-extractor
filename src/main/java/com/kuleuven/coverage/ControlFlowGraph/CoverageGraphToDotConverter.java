package com.kuleuven.coverage.ControlFlowGraph;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.AbstAstEdge;
import sootup.codepropertygraph.propertygraph.edges.AbstCdgEdge;
import sootup.codepropertygraph.propertygraph.edges.AbstDdgEdge;
import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;
import sootup.codepropertygraph.propertygraph.nodes.AggregateGraphNode;
import sootup.codepropertygraph.propertygraph.nodes.ModifierGraphNode;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;
import sootup.codepropertygraph.propertygraph.nodes.StmtGraphNode;
import sootup.codepropertygraph.propertygraph.nodes.TypeGraphNode;

/*
Logic referenced of PropertyGraphToDotConverter of SootUp 2.0.
*/
public class CoverageGraphToDotConverter {
    public static String convert(PropertyGraph graph) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("digraph %s {\n", graph.getName()));
        builder.append("\trankdir=TB;\n");
        builder.append("\tnode [style=filled, shape=record];\n");
        builder.append("\tedge [style=filled]\n");
        Map<String, String> nodeIds = new LinkedHashMap<>();
        AtomicInteger nodeIdCounter = new AtomicInteger(1);

        for (PropertyGraphNode node : graph.getNodes().stream().sorted(Comparator.comparing(PropertyGraphNode::toString)).toList()) {
            String nodeId = nodeIds.computeIfAbsent(node.toString(), (k) -> String.valueOf(nodeIdCounter.getAndIncrement()));
            String label = getNodeLabel(node);
            String color = getNodeColor(node);
            builder.append(String.format("\t\"%s\" [label=\"%s\", fillcolor=\"%s\"];\n", nodeId, label, color));
        }

        for (PropertyGraphEdge edge : graph.getEdges().stream()
                .sorted(Comparator.comparing((PropertyGraphEdge e) -> nodeIds.get(e.getSource().toString()))
                        .thenComparing((e) -> nodeIds.get(e.getDestination().toString()))
                        .thenComparing(PropertyGraphEdge::getLabel)).toList()) {
            String sourceId = nodeIds.get(edge.getSource().toString());
            String destinationId = nodeIds.get(edge.getDestination().toString());
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

    private static String getNodeLabel(PropertyGraphNode node) {
        return escapeDot(node.toString());
    }

    private static String getNodeColor(PropertyGraphNode node) {
        if (node instanceof StmtGraphNode) {
            return "lightblue";
        } else if (!(node instanceof TypeGraphNode) && !(node instanceof ModifierGraphNode)) {
            return node instanceof AggregateGraphNode ? "darkseagreen2" : "white";
        } else {
            return "lightgray";
        }
    }

    private static String getEdgeColor(PropertyGraphEdge edge) {
        if (edge instanceof AbstAstEdge) {
            return "darkseagreen4";
        } else if (edge instanceof AbstCdgEdge) {
            return "dodgerblue4";
        } else {
            return edge instanceof AbstDdgEdge ? "firebrick" : "black";
        }
    }
}
