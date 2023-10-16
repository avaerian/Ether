package org.minerift.ether.database.sql.adapters;

// from, to
public interface Adapter<F, T> {

    T adaptTo(F obj);
    F adaptFrom(T obj);

}
