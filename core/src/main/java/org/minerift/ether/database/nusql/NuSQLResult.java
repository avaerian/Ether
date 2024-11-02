package org.minerift.ether.database.nusql;

import org.jetbrains.annotations.NotNull;
import org.jooq.Record;
import org.jooq.Result;
import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;
import org.minerift.ether.util.IBuilder;

import java.sql.ResultSet;
import java.util.Iterator;
import java.util.stream.Stream;

import static org.minerift.ether.database.nusql.SQLUtils.asJooqField;

public class NuSQLResult<MO> implements Iterable<MO> {
    private final Model<MO, ?> model;
    private final Result<Record> result;

    public NuSQLResult(Model<MO, ?> model, Result<Record> result) {
        this.model = model;
        this.result = result;
    }

    public Result<Record> asJooqResult() {
        return result;
    }

    public ResultSet asResultSet() {
        return result.intoResultSet();
    }

    public boolean isEmpty() {
        return result.isEmpty();
    }

    public int size() {
        return result.size();
    }

    public Model<MO, ?> getModel() {
        return model;
    }

    public Record getRecord(int idx) {
        return result.get(idx);
    }

    public <T> T readField(Field<MO, T, ?> field, Record record) {
        Object sqlVal = record.get(asJooqField(field));
        System.out.println("readField sqlVal: " + sqlVal); // debug
        return field.readSQLAsJavaValue(sqlVal);
    }

    public <T> T readField(Field<MO, T, ?> field, int idx) {
        return readField(field, result.get(idx));
    }

    public <C, T> C readField(Field.FieldWithAdapter<MO, C, T, ?> field, Record record) {
        return field.adapter.adaptFrom(readField((Field<MO, T, ?>) field, record));
    }

    public <C, T> C readField(Field.FieldWithAdapter<MO, C, T, ?> field, int idx) {
        return readField(field, result.get(idx));
    }

    public MO readRecord(Record record) {
        return model.readRecord(this, record);
    }

    public MO readRecord(int idx) {
        return readRecord(result.get(idx));
    }

    public <B extends IBuilder<MO>> B readBuilder(Record record) {
        return (B) model.readAsBuilder(this, record);
    }

    public <B extends IBuilder<MO>> B readBuilder(int idx) {
        return readBuilder(getRecord(idx));
    }

    public Stream<Record> streamRecords() {
        return result.stream();
    }

    public Iterator<Record> iterateRecords() {
        return result.iterator();
    }

    public <B extends IBuilder<MO>> Stream<B> streamBuilders() {
        return result.stream().map(this::readBuilder);
    }

    public <B extends IBuilder<MO>> Iterator<B> iterateBuilders() {
        return (Iterator<B>) streamBuilders().iterator();
    }

    public <T> Stream<T> streamField(Field<MO, T, ?> field) {
        return streamRecords().map(record -> readField(field, record));
    }

    public Stream<MO> stream() {
        return result.stream().map(this::readRecord);
    }

    @Override
    public @NotNull Iterator<MO> iterator() {
        return stream().iterator();
    }
}
