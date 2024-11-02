package org.minerift.ether.database.nusql.fallback;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.nusql.adapters.Adapter;

public abstract class NuFallback<T, F> implements Adapter<T, F> {

    private final DataType<F> type;

    public NuFallback(DataType<F> type) {
        this.type = type;
    }

    public DataType<F> getDataType() {
        return type;
    }
}
