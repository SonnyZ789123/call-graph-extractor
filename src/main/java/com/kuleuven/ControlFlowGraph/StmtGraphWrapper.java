package com.kuleuven.ControlFlowGraph;

import sootup.core.graph.StmtGraph;

public class StmtGraphWrapper {
    private final StmtGraph<?> stmtGraph;

    public StmtGraphWrapper(StmtGraph<?> stmtGraph) {
        this.stmtGraph = stmtGraph;
    }

    public void someMethod() {
        // Implementation goes here
        stmtGraph.getNodes();
        
    }
}
