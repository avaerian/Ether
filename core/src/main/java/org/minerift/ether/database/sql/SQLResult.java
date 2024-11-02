package org.minerift.ether.database.sql;

import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.jooq.Result;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.util.IBuilder;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.stream.Stream;

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

    public ResultSet asResultSet() {
        return result.intoResultSet();
    }

    public int size() {
        return result.size();
    }

    public boolean isEmpty() {
        return result.isEmpty();
    }

    public <T> T readField(Field<M, T, ?> field, Record record) {
        Object sqlVal = record.get(field.asJooqField());
        System.out.println("readField sqlVal: " + sqlVal);
        return field.readSQLAsJavaValue(sqlVal);
    }

    public <T> T readField(Field<M, T, ?> field, int idx) {
        return readField(field, result.get(idx));
    }

    public <T, R> R readField(Field.FieldWithAdapter<M, T, R, ?> field, Record record) {
        return field.adapter.adaptFrom(readField((Field<M, T, ?>) field, record));
    }

    public <T, R> R readField(Field.FieldWithAdapter<M, T, R, ?> field, int idx) {
        return readField(field, result.get(idx));
    }

    public M readRecord(Record record) {
        return model.readResult(this, record);
    }

    public <B extends IBuilder<M>> B readBuilder(Record record) {
        return (B) model.readAsBuilder(this, record);
    }

    public Stream<Record> stream() {
        return result.stream();
    }

    @NotNull
    @Override
    public Iterator<Record> iterator() {
        return result.iterator();
    }
}
