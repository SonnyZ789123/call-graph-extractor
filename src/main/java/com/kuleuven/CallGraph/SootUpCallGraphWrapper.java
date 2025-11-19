package com.kuleuven.CallGraph;

import sootup.callgraph.CallGraph;
import sootup.core.signatures.MethodSignature;

import java.util.Set;

public class SootUpCallGraphWrapper implements ICallGraph<MethodSignature> {
    CallGraph callGraph;
    Set<MethodSignature> nodes;
    Set<SootUpCallWrapper> edges;

    public SootUpCallGraphWrapper(CallGraph callGraph) {
        this.callGraph = callGraph;
        this.nodes = callGraph.getMethodSignatures();
        this.edges = callGraph.getCalls().stream()
                .map(SootUpCallWrapper::new)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public Set<MethodSignature> getNodes() {
        return this.nodes;
    }

    @Override
    public Set<? extends Edge<MethodSignature>> getEdges() {
        return this.edges;
    }

    @Override
    public Set<? extends Edge<MethodSignature>> callsFrom(MethodSignature node) {
        return callGraph.callsFrom(node).stream()
                .map(SootUpCallWrapper::new)
                .collect(java.util.stream.Collectors.toSet());
    }

    @Override
    public Set<? extends Edge<MethodSignature>> callsTo(MethodSignature node) {
        return callGraph.callsTo(node).stream()
                .map(SootUpCallWrapper::new)
                .collect(java.util.stream.Collectors.toSet());
    }

    private static class SootUpCallWrapper implements ICallGraph.Edge<MethodSignature> {
        CallGraph.Call call;

        private SootUpCallWrapper(CallGraph.Call call) {
            this.call = call;
        }

        @Override
        public MethodSignature getSource() {
            return call.sourceMethodSignature();
        }

        @Override
        public MethodSignature getTarget() {
            return call.targetMethodSignature();
        }
    }
}
