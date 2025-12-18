package com.kuleuven.coverage.ExecutionPathTracker;

public record BlockInfo(
        String className,
        String methodDescriptor,
        String stmt,
        int bytecodeOffset
) {}
