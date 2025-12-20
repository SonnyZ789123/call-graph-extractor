package com.kuleuven.coverage.CoverageAgent;

import sootup.core.jimple.common.stmt.Stmt;

public class SootStmtGraphUtil {
    /**
     * Get a unique identifier inside a method for a statement based on its position and string representation.
     * @param stmt The statement to identify.
     * @return A unique identifier string for the statement.
     */
    public static String getStmtId(Stmt stmt) {
        return stmt.getPositionInfo().getStmtPosition().toString() + "||" + stmt.toString();
    }
}
