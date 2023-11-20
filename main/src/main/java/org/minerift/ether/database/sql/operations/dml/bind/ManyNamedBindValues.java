package org.minerift.ether.database.sql.operations.dml.bind;

import java.util.Map;

public class ManyNamedBindValues<T> implements NamedBindValues<T> {

    private final Map<String, T> namedBindVals;

    public ManyNamedBindValues(Map<String, T> namedBindVals) {
        this.namedBindVals = namedBindVals;
    }

    @Override
    public T getField(String field) {
        return namedBindVals.get(field);
    }
}
