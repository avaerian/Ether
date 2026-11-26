package org.minerift.ether.island;

public enum PurgeIslandsOption {
    THRESHOLD,
    QUEUED,
    LAZY,

    ;

    public static PurgeIslandsOption valueOfSilent(String str) {
        return switch (str.toUpperCase()) {
            case "THRESHOLD" -> THRESHOLD;
            case "QUEUED" -> QUEUED;
            case "LAZY" -> LAZY;
            default -> null;
        };
    }
}
