package com.kuleuven.helpers;

import sootup.callgraph.CallGraph;

public class DotHelper {
    static public String getDotIdFromSootUpCall(CallGraph.Call call) {
        return "\"" + call.sourceMethodSignature() + "\"->\"" + call.targetMethodSignature() + "\"";
    }
}
