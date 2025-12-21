package com.kuleuven.ICFG;

import com.kuleuven.CallGraph.ICallGraph;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootMethod;

import java.util.Map;

public class ICFG {
    public ICallGraph<MethodSignature> callGraph;
    public Map<JavaSootMethod, ControlFlowGraph<?>> methodToCFGMap;

    public ICFG(ICallGraph<MethodSignature> callGraph, Map<JavaSootMethod, ControlFlowGraph<?>> methodToCFGMap) {
        this.callGraph = callGraph;
        this.methodToCFGMap = methodToCFGMap;
    }
}
