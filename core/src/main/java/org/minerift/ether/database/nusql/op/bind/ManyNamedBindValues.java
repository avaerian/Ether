package org.minerift.ether.database.nusql.op.bind;

import java.util.Map;

public class ManyNamedBindValues<T> implements NamedBindValues<T> {

    private final Map<String, T> namedBindVals;

    public ManyNamedBindValues(Map<String, T> namedBindVals) {
        this.namedBindVals = namedBindVals;
    }

    @Override
    public T getFieldValue(String field) {
        return namedBindVals.get(field);
    }

    @Override
    public Map<String, T> asMap() {
        return namedBindVals;
    }
}
