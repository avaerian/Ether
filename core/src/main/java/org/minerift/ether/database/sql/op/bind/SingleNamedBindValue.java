package org.minerift.ether.database.sql.op.bind;

import java.util.Map;

public class SingleNamedBindValue<V> implements NamedBindValues<V> {

    private final String field;
    private final V bindVal;

    public SingleNamedBindValue(String field, V bindVal) {
        this.field = field;
        this.bindVal = bindVal;
    }

    @Override
    public V getFieldValue(String field) {
        return this.field.equals(field) ? bindVal : null;
    }

    @Override
    public Map<String, V> asMap() {
        return Map.of(field, bindVal);
    }
}
