package org.minerift.ether.database.sql;

import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.jooq.Result;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;

import java.util.Iterator;

public class SQLResult<M> implements Iterable<Record> {

    private final Model<M, ?> model;
    private final Result<Record> result;

    public SQLResult(Model<M, ?> model, Result<Record> result) {
        this.model = model;
        this.result = result;
    }

    public Result<Record> asJooqResult() {
        return result;
    }

    public boolean isEmpty() {
        return result.isEmpty();
    }

    public <T> T getField(Field<M, T, ?> field, Record record) {
        return field.readSQLValue(record.get(field.getName()));
    }

    public <T> T getField(Field<M, T, ?> field, int idx) {
        return getField(field, result.get(idx));
    }

    public <T, R> R getField(Field.FieldWithAdapter<M, T, R, ?> field, Record record) {
        return field.adapter.adaptFrom(getField((Field<M, ? extends T, ?>) field, record));
    }

    public <T, R> R getField(Field.FieldWithAdapter<M, T, R, ?> field, int idx) {
        return getField(field, result.get(idx));
    }

    public M readRecord(Record record) {
        return model.readResult(this, record);
    }

    @NotNull
    @Override
    public Iterator<Record> iterator() {
        return result.iterator();
    }
}
