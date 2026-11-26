package org.minerift.ether.database;

import org.minerift.ether.database.fallback.Fallback;
import org.minerift.ether.database.adapters.Adapter;
import org.minerift.ether.util.Utils;

import java.lang.ref.WeakReference;
import java.util.function.Function;

// MO is Model Object (class that is being modeled, not Model extended class itself)
// T is SQL data type
public class Field<MO, T> {
    protected final WeakReference<Model<MO, ?>> model;
    protected final String name;
    protected final DataType<T> requestedDataType; // original type that may need a fallback
    protected final Function<MO, ?> objFieldReader;
    protected final Fallback<T, ?> fallback; // safe data type supported across all dialects
    protected Class<?> fallbackClazz;


    @Deprecated
    private Class<? extends Model> getFieldCreator() {
        StackWalker.StackFrame stackFrame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(s -> s.filter(sf -> Model.class.isAssignableFrom(sf.getDeclaringClass()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException(getName() + " not associated with any Model"))
                );
        return (Class<? extends Model>) stackFrame.getDeclaringClass();
    }

    protected Field(String name, DataType<T> type, Model<MO, ?> model, Function<MO, ?> objFieldReader, Fallback<T, ?> fallback) {
        this.name = name;
        this.requestedDataType = type;
        this.model = new WeakReference<>(model);
        this.objFieldReader = objFieldReader;
        this.fallback = fallback;
    }

    public String getName() {
        return name;
    }

    public boolean usesFallbackType() {
        return fallback != null;
    }

    public DataType<?> getDataType() {
        return usesFallbackType() ? fallback.getDataType() : requestedDataType;
    }

    // TODO: actual docs; this returns the original type. getDataType() returns the type in use
    public DataType<T> getRequestedDataType() {
        return requestedDataType;
    }

    public Model<MO, ?> getOwner() {
        return model.get();
    }

    public WeakReference<Model<MO, ?>> getModelReal() {
        return model;
    }

    public T readField(MO obj) {
        return (T) objFieldReader.apply(obj);
    }

    /**
     * Reads this field from the object as either the SQL data type or fallback, depending on the dialect and data type.
     * @param obj instance of object being modeled
     * @return value as data type or fallback
     */
    public Object readAsSQLValue(MO obj) {
        return usesFallbackType() ? fallback.adaptTo(readField(obj)) : readField(obj);
    }

    // Reads java field value as SQL value (either SQL fallback or original type)
    public Object readJavaAsSQLValue(Object javaVal) {
        return usesFallbackType() && requestedDataType.getType().isInstance(javaVal) ? fallback.adaptTo((T) javaVal) : javaVal;
    }

    // Takes SQL data and converts it from fallback to proper SQL data type, if appropriate
    // TODO: review
    public T readSQLAsJavaValue(Object sqlVal) {
        Object fixedSqlVal = sqlVal instanceof String sqlStr ? Utils.fixString(sqlStr) : sqlVal; // TODO: remove/refactor fix up
        return fallback != null ? ((Fallback<T, Object>)fallback).adaptFrom(fixedSqlVal) : (T) fixedSqlVal;
    }

    // TODO: rename and create additional methods for getting field data type
    public Class<?> getSQLDataType() {
        return getDataType().getType();
    }

    @Override
    public String toString() {
        return "Field{" + getName() + ", model=" + model.get().getClass().getSimpleName() + "}";
    }

    public <C> Field.FieldWithAdapter<MO, C, T> asComplexField() throws ClassCastException {
        if(!(this instanceof Field.FieldWithAdapter<?,?,?> fieldWithAdapter)) { // TODO: test with new generics
            throw new ClassCastException("Attempted to cast a db field as a complex (adapted) db field");
        }
        return (FieldWithAdapter<MO, C, T>) fieldWithAdapter;
    }

    @Deprecated
    public <C> Field.FieldWithAdapter<MO, C, T> asComplexField(Class<C> complexTypeClazz) throws ClassCastException {
        return asComplexField();
    }

    // Fields with types that need to be adapted are handled here
    // R is Complex result
    public static class FieldWithAdapter<M, C, T> extends Field<M, T> {
        public final Adapter<C, T> adapter;

        protected FieldWithAdapter(String name, DataType<T> type, Model<M, ?> model, Function<M, C> objFieldReader, Adapter<C, T> adapter, Fallback<T, ?> fallback) {
            super(name, type, model, objFieldReader, fallback);
            this.adapter = adapter;
        }

        @Override
        public T readField(M obj) {
            return adapter.adaptTo((C) objFieldReader.apply(obj));
        }
    }
}