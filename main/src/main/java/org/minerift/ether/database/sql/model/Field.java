package org.minerift.ether.database.sql.model;

import org.jooq.DataType;
import org.minerift.ether.database.sql.adapters.Adapter;
import org.minerift.ether.database.sql.fallback.Fallback;
import org.minerift.ether.util.Utils;

import java.util.function.Function;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.name;

// M is Model (class that is being modeled, not model class itself)
// T is SQL data type
// F is fallback SQL data type
public class Field<M, T, F> {

    protected final org.jooq.Field<?> field;
    protected final Function<M, ?> objFieldReader;
    protected final Fallback<T, F> fallback; // safe data type supported across all dialects

    protected Field(String name, DataType<T> type, Function<M, ?> objFieldReader) {
        this(name, type, objFieldReader, null);
    }

    protected Field(String name, DataType<T> type, Function<M, ?> objFieldReader, Fallback<T, F> fallback) {

        this.field = fallback == null
                ? field(name(name), type)
                : field(name(name), fallback.getDataType());

        //System.out.println(getSQLDataType().getName()); // debug

        this.objFieldReader = objFieldReader;
        this.fallback = fallback;
    }

    public String getName() {
        return field.getName();
    }

    public org.jooq.Field<?> asJooqField() {
        return field;
    }

    public T readField(M obj) {
        return (T) objFieldReader.apply(obj);
    }

    // Reads the field from the object as an SQL value (data type)
    public Object readAsSQLValue(M obj) {
        return fallback != null ? fallback.adaptTo(readField(obj)) : readField(obj);
    }

    // Reads java field value as SQL value (either SQL fallback or original type)
    public Object readJavaAsSQLValue(Object javaVal) {
        return fallback != null ? fallback.adaptTo((T) javaVal) : javaVal;
    }

    // Takes SQL data and converts it from fallback to proper SQL data type, if appropriate
    public T readSQLAsJavaValue(Object sqlVal) {
        Object fixedSqlVal = sqlVal instanceof String sqlStr ? Utils.fixString(sqlStr) : sqlVal; // TODO: remove/refactor fix up
        return fallback != null ? fallback.adaptFrom((F) fixedSqlVal) : (T) fixedSqlVal;
    }

    public Class<?> getSQLDataType() {
        return field.getType();
    }

    public <R> FieldWithAdapter<M, T, R, F> asComplexField() {
        if(!(this instanceof Field.FieldWithAdapter<?,?,?,?> fieldWithAdapter)) {
            throw new UnsupportedOperationException("Attempted to cast a db field as a complex (adapted) db field!");
        }
        return (FieldWithAdapter<M, T, R, F>) fieldWithAdapter;
    }

    public <R> FieldWithAdapter<M, T, R, F> asComplexField(Class<R> complexTypeClazz) {
        return asComplexField();
    }

    // R is Complex result
    public static class FieldWithAdapter<M, T, R, F> extends Field<M, T, F> {
        public final Adapter<R, T> adapter;

        protected FieldWithAdapter(String name, DataType<T> type, Function<M, R> objFieldReader, Adapter<R, T> adapter) {
            this(name, type, objFieldReader, adapter,null);
        }

        protected FieldWithAdapter(String name, DataType<T> type, Function<M, R> objFieldReader, Adapter<R, T> adapter, Fallback<T, F> fallback) {
            super(name, type, objFieldReader, fallback);
            this.adapter = adapter;
        }

        @Override
        public T readField(M obj) {
            return adapter.adaptTo((R) objFieldReader.apply(obj));
        }
    }
}