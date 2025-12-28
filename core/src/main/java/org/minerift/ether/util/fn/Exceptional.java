package org.minerift.ether.util.fn;

@FunctionalInterface
public interface Exceptional {
    void run() throws Exception;
}