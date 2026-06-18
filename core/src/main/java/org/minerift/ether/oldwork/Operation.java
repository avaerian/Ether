package org.minerift.ether.oldwork;

public record Operation(String name, Runnable task) {
    // optional for syntax style
    public static Operation of(String name, Runnable task) {
        return new Operation(name, task);
    }
}
