package com.kuleuven.coverage.ExecutionPathTracker;

import java.util.BitSet;

public final class CoverageRuntime {
    private static final BitSet blocks = new BitSet();

    public static void hit(int blockId) {
        blocks.set(blockId);
    }

    public static BitSet snapshot() {
        return (BitSet) blocks.clone();
    }

    public static void reset() {
        blocks.clear();
    }
}

