package com.kuleuven.coverage.ControlFlowGraph.Graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kuleuven.coverage.ControlFlowGraph.CoverageGraphToDotConverter;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;
import sootup.codepropertygraph.propertygraph.nodes.PropertyGraphNode;
import sootup.codepropertygraph.propertygraph.util.PropertyGraphToDotConverter;

public final class CFGCoverageGraph implements PropertyGraph {
    private final String name;
    private final List<PropertyGraphNode> nodes;
    private final List<PropertyGraphEdge> edges;

    private CFGCoverageGraph(String name, List<PropertyGraphNode> nodes, List<PropertyGraphEdge> edges) {
        this.name = name;
        this.nodes = Collections.unmodifiableList(nodes);
        this.edges = Collections.unmodifiableList(edges);
    }

    public String getName() {
        return this.name;
    }

    public List<PropertyGraphNode> getNodes() {
        return this.nodes;
    }

    public List<PropertyGraphEdge> getEdges() {
        return this.edges;
    }

    public String toDotGraph() {
        return CoverageGraphToDotConverter.convert(this);
    }

    public static class Builder implements PropertyGraph.Builder {
        private final List<PropertyGraphNode> nodes = new ArrayList<>();
        private final List<PropertyGraphEdge> edges = new ArrayList<>();
        private String name;

        public com.kuleuven.coverage.ControlFlowGraph.Graph.CFGCoverageGraph.Builder setName(String name) {
            this.name = name;
            return this;
        }

        public com.kuleuven.coverage.ControlFlowGraph.Graph.CFGCoverageGraph.Builder addNode(PropertyGraphNode node) {
            if (!(node instanceof CoverageNode)) {
                throw new IllegalArgumentException("Graph can only contain coverage nodes");
            } else {
                if (!this.nodes.contains(node)) {
                    this.nodes.add(node);
                } else {
                    System.out.println("already contains node: " + ((CoverageNode) node).getBlockInfo());
                }

                return this;
            }
        }

        public com.kuleuven.coverage.ControlFlowGraph.Graph.CFGCoverageGraph.Builder addEdge(PropertyGraphEdge edge) {
            this.addNode(edge.getSource());
            this.addNode(edge.getDestination());
            if (!this.edges.contains(edge)) {
                this.edges.add(edge);
            } else {
                System.out.println("already contains edge: [" + edge.getSource().toString() + ", " + edge.getLabel() + ", " + edge.getDestination().toString() + "]");
            }

            return this;
        }

        public PropertyGraph build() {
            return new com.kuleuven.coverage.ControlFlowGraph.Graph.CFGCoverageGraph(this.name, this.nodes, this.edges);
        }
    }
}
