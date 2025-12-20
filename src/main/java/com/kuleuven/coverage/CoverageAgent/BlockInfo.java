package com.kuleuven.coverage.CoverageAgent;

public record BlockInfo(
        String className,
        String methodName,
        String methodDescriptor,
        String stmtId,
        int lineNumber
) {}
