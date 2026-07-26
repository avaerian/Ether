package org.minerift.ether.database.fallback;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.adapters.Adapter;

public abstract class Fallback<T, F> implements Adapter<T, F> {

    private final DataType<F> type;

    public Fallback(DataType<F> type) {
        this.type = type;
    }

    public DataType<F> getDataType() {
        return type;
    }
}
