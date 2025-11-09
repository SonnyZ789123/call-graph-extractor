/*
Slightly modified from Olav Blaak's implementation, please refer to:
https://github.com/olavblaak1/graph-based-coverage-improvement/blob/main/src/main/java/com/kuleuven/GraphAnalyzer/MetricAnalyzer/Metric/PageRankMetric.java
*/

package com.kuleuven.metrics;

import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PageRank {

    // Stores the final PageRank score for each method in the call graph.
    private final Map<MethodSignature, Double> pageRankScores = new HashMap<>();

    // The call graph to analyze (directed graph of method calls).
    private final CallGraph callGraph;

    // Maximum number of PageRank iterations (upper bound to prevent infinite loops).
    private static final int MAX_ITERATIONS = 100;

    // Probability of "following" a call edge vs. randomly jumping (standard PageRank constant).
    private static final double DAMPING_FACTOR = 0.85;

    // Minimum change required between iterations to consider convergence.
    private static final double TOLERANCE = 1.0e-6;

    public PageRank(CallGraph callGraph) {
        this.callGraph = callGraph;
    }

    /**
     * Returns the PageRank score for a given method.
     * The PageRank computation is performed lazily on first request.
     */
    public double calculateRank(MethodSignature node) {
        if (pageRankScores.isEmpty()) {
            computePageRank();
        }
        return pageRankScores.getOrDefault(node, 0.0);
    }

    /**
     * Computes PageRank scores for all methods in the call graph.
     * This treats the call graph as a directed graph where edges represent "method A calls B".
     * Methods with many important callers receive higher PageRank.
     */
    private void computePageRank() {

        // Collect all methods (nodes) in the call graph.
        Set<MethodSignature> nodes = callGraph.getMethodSignatures();
        int n = nodes.size();

        // Initialize uniform probability distribution.
        Map<MethodSignature, Double> scores = new HashMap<>();
        for (MethodSignature node : nodes) {
            scores.put(node, 1.0 / n);
        }

        // Perform power-iteration until stable or max iterations reached.
        for (int iter = 0; iter < MAX_ITERATIONS; iter++) {

            Map<MethodSignature, Double> newScores = new HashMap<>();
            double danglingScore = 0.0;

            // A "dangling node" has no outgoing calls.
            // Its rank is distributed evenly across the entire graph.
            for (MethodSignature node : nodes) {
                if (callGraph.callsFrom(node).isEmpty()) {
                    danglingScore += scores.get(node);
                }
            }

            boolean converged = true;

            // Update rank scores based on incoming edges.
            for (MethodSignature node : nodes) {

                // Sum contributions from all callers of this node.
                double incomingSum = 0.0;
                for (CallGraph.Call incomingEdge : callGraph.callsTo(node)) {

                    MethodSignature sourceNode = incomingEdge.sourceMethodSignature();
                    int outDegree = callGraph.callsFrom(sourceNode).size();

                    // Each caller divides its rank among the methods it calls.
                    if (outDegree > 0) {
                        incomingSum += scores.get(sourceNode) / outDegree;
                    }
                }

                /*
                 * PageRank update formula:
                 * (1 - d)/N  → random jump probability
                 * d * incomingSum → passed rank from callers
                 * d * (danglingScore / N) → redistributing rank from dead ends
                 */
                double rank = (1.0 - DAMPING_FACTOR) / n;
                rank += DAMPING_FACTOR * (incomingSum + (danglingScore / n));
                newScores.put(node, rank);

                // Track convergence
                if (Math.abs(rank - scores.get(node)) > TOLERANCE) {
                    converged = false;
                }
            }

            scores = newScores;

            if (converged) {
                break; // Stop early if ranks do not change significantly
            }
        }

        pageRankScores.clear();
        pageRankScores.putAll(scores);
    }
}
