package org.minerift.ether.database.sql.fallback;

import org.jooq.DataType;
import org.minerift.ether.database.sql.adapters.Adapter;

public abstract class Fallback<T, F> implements Adapter<T, F> {

    private final DataType<F> type;

    public Fallback(DataType<F> type) {
        this.type = type;
    }

    public DataType<F> getDataType() {
        return type;
    }
}
