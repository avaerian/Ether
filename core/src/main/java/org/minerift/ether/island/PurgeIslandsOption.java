package org.minerift.ether.island;

public enum PurgeIslandsOption {
    THRESHOLD,
    QUEUED,
    INSTANT,

    ;

    public static PurgeIslandsOption valueOfSilent(String str) {
        return switch (str.toUpperCase()) {
            case "THRESHOLD" -> THRESHOLD;
            case "QUEUED" -> QUEUED;
            case "INSTANT" -> INSTANT;
            default -> null;
        };
    }
}
