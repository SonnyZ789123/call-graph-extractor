package com.kuleuven.metrics;

public enum CallGraphConstructionAlgorithm {
    CHA, RTA;

    /**
     * Parses a string into a CallGraphAlgorithm (case-insensitive).
     * Returns null or throws if the value is invalid.
     */
    public static CallGraphConstructionAlgorithm fromString(String value) {
        if (value == null) return null;
        switch (value.trim().toLowerCase()) {
            case "cha" -> { return CHA; }
            case "rta" -> { return RTA; }
            default -> throw new IllegalArgumentException("Unknown algorithm: " + value);
        }
    }
}
