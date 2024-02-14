package org.minerift.ether.database.sql.op.dml.bind;

public class SingleNamedBindValue<V> implements NamedBindValues<V> {

    private final String field;
    private final V bindVal;

    public SingleNamedBindValue(String field, V bindVal) {
        this.field = field;
        this.bindVal = bindVal;
    }

    @Override
    public V getField(String field) {
        return this.field.equals(field) ? bindVal : null;
    }
}
