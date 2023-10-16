package org.minerift.ether.database.sql.model;

import org.jooq.DataType;
import org.minerift.ether.database.sql.adapters.Adapter;

import java.util.function.Function;

import static org.jooq.impl.DSL.field;

public class Field<M, T> {
    protected final org.jooq.Field<T> field;
    protected final Function<M, ?> objFieldReader;

    protected Field(String name, DataType<T> type, Function<M, ?> objFieldReader) {
        this.field = field(name, type);
        this.objFieldReader = objFieldReader;
    }

    public org.jooq.Field<T> getSQLField() {
        return field;
    }

    public T read(M obj) {
        return (T) objFieldReader.apply(obj);
    }

    public Class<T> getSQLDataType() {
        return field.getType();
    }

    protected static class FieldWithAdapter<M, T, R> extends Field<M, T> {
        protected final Adapter<R, T> adapter;

        protected FieldWithAdapter(String name, DataType<T> type, Function<M, R> objFieldReader, Adapter<R, T> adapter) {
            super(name, type, objFieldReader);
            this.adapter = adapter;
        }

        @Override
        public T read(M obj) {
            return adapter.adaptTo((R) objFieldReader.apply(obj));
        }
    }
}