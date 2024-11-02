package org.minerift.ether.database.nusql.adapters;

// F - from
// T - to
public interface Adapter<F, T> {
    T adaptTo(F obj);
    F adaptFrom(T obj);
}
