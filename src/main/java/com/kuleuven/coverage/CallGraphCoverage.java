package com.kuleuven.coverage;

import com.kuleuven.helpers.DotHelper;
import sootup.callgraph.CallGraph;
import sootup.callgraph.CallGraph.Call;
import sootup.core.signatures.MethodSignature;

import java.util.HashMap;
import java.util.Map;

public class CallGraphCoverage {

    /**
     * Computes how often methods (nodes) and calls (edges) from the source call graph
     * are covered by the test call graph.
     *
     * Coverage is measured as counts — how many times a node or edge appears in the test graph.
     */
    public CoverageResult calculateCoverage(CallGraph sourceCallGraph, CallGraph testCallGraph) {

        // Initialize coverage maps for all source nodes & edges
        Map<String, Integer> nodeCoverage = new HashMap<>();
        Map<String, Integer> edgeCoverage = new HashMap<>();

        for (MethodSignature m : sourceCallGraph.getMethodSignatures()) {
            nodeCoverage.put(m.toString(), 0);
        }

        for (Call srcCall : sourceCallGraph.getCalls()) {
            String edgeKey = edgeKey(srcCall);
            edgeCoverage.put(edgeKey, 0);
        }

        // Count coverage — for each call in the test graph, map to the source graph
        for (Call testCall : testCallGraph.getCalls()) {
            MethodSignature src = testCall.sourceMethodSignature();
            MethodSignature dst = testCall.targetMethodSignature();
            String edgeKey = edgeKey(testCall);

            // Node coverage: mark both source and destination as covered
            nodeCoverage.computeIfPresent(src.toString(), (k, v) -> v + 1);
            nodeCoverage.computeIfPresent(dst.toString(), (k, v) -> v + 1);

            // Edge coverage: increment if this edge exists in the source
            edgeCoverage.computeIfPresent(edgeKey, (k, v) -> v + 1);
        }

        return new CoverageResult(nodeCoverage, edgeCoverage);
    }

    /** Helper: unique key for an edge (call) */
    private String edgeKey(Call call) {
        return DotHelper.getDotIdFromSootUpCall(call);
    }

    /** Holds node and edge coverage maps */
    public static class CoverageResult {
        private final Map<String, Integer> nodeCoverage;
        private final Map<String, Integer> edgeCoverage;

        public CoverageResult(Map<String, Integer> nodeCoverage,
                              Map<String, Integer> edgeCoverage) {
            this.nodeCoverage = nodeCoverage;
            this.edgeCoverage = edgeCoverage;
        }

        public Map<String, Integer> getNodeCoverage() {
            return nodeCoverage;
        }

        public Map<String, Integer> getEdgeCoverage() {
            return edgeCoverage;
        }

        /** Fraction of covered nodes */
        public double getNodeCoverageRatio() {
            long covered = nodeCoverage.values().stream().filter(v -> v > 0).count();
            return (double) covered / nodeCoverage.size();
        }

        /** Fraction of covered edges */
        public double getEdgeCoverageRatio() {
            long covered = edgeCoverage.values().stream().filter(v -> v > 0).count();
            return (double) covered / edgeCoverage.size();
        }

        public void printSummary() {
            System.out.printf("✅ Node coverage: %.2f%% (%d/%d)%n",
                    getNodeCoverageRatio() * 100,
                    nodeCoverage.values().stream().filter(v -> v > 0).count(),
                    nodeCoverage.size());

            System.out.printf("✅ Edge coverage: %.2f%% (%d/%d)%n",
                    getEdgeCoverageRatio() * 100,
                    edgeCoverage.values().stream().filter(v -> v > 0).count(),
                    edgeCoverage.size());
        }
    }
}
