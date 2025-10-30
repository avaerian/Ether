package org.minerift.ether.util.fn;

@FunctionalInterface
public interface Exceptional<E extends Exception> {
    void run() throws E;
}