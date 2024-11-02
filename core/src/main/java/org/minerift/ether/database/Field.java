package org.minerift.ether.database;

import org.minerift.ether.database.nusql.fallback.NuFallback;
import org.minerift.ether.database.nusql.adapters.Adapter;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.Utils;

import java.util.function.Function;

// MO is Model Object (class that is being modeled, not model class itself)
// T is SQL data type
// F is fallback SQL data type
public class Field<MO, T, F> {

    protected final Class<? extends Model> creatorClazz;
    protected final String name;
    protected final DataType<T> requestedDataType; // original type that may need a fallback
    protected final Function<MO, ?> objFieldReader;
    protected final NuFallback<T, F> fallback; // safe data type supported across all dialects


    private Class<? extends Model> getFieldCreator() {
        StackWalker.StackFrame stackFrame = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
                .walk(s -> s.filter(sf -> Model.class.isAssignableFrom(sf.getDeclaringClass()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException(getName() + " not associated with any Model"))
                );
        return (Class<? extends Model>) stackFrame.getDeclaringClass();
    }

    protected Field(String name, DataType<T> type, Function<MO, ?> objFieldReader, NuFallback<T, F> fallback) {
        // Get class that called this ctor
        // If class is a model-type, set creatorClazz
        // TODO: Else, set to null and log to user about this field being a debug or unit testing field
        this.name = name;
        this.requestedDataType = type;
        this.creatorClazz = getFieldCreator();

        System.out.println(name + ": " + getSQLDataType().getName() + ", " + creatorClazz); // debug

        this.objFieldReader = objFieldReader;
        this.fallback = fallback;
    }

    @Debug
    private Field(String name, DataType<T> type, Function<MO, ?> objFieldReader, NuFallback<T, F> fallback, Class<? extends Model> creatorClazz) {
        this.name = name;
        this.creatorClazz = creatorClazz;
        this.requestedDataType = type;

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
        return usesFallbackType() ? fallback.adaptTo((T) javaVal) : javaVal;
    }

    // Takes SQL data and converts it from fallback to proper SQL data type, if appropriate
    // TODO: review
    public T readSQLAsJavaValue(Object sqlVal) {
        Object fixedSqlVal = sqlVal instanceof String sqlStr ? Utils.fixString(sqlStr) : sqlVal; // TODO: remove/refactor fix up
        return fallback != null ? fallback.adaptFrom((F) fixedSqlVal) : (T) fixedSqlVal;
    }

    // TODO: rename and create additional methods for getting field data type
    public Class<?> getSQLDataType() {
        return getDataType().getType();
    }

    @Override
    public String toString() {
        return "Field{" + getName() + ", model=" + creatorClazz.getSimpleName() + "}";
    }

    // TODO: review both methods below
    public <C> Field.FieldWithAdapter<MO, C, T, F> asComplexField() throws ClassCastException {
        if(!(this instanceof Field.FieldWithAdapter<?,?,?,?> fieldWithAdapter)) {
            throw new ClassCastException("Attempted to cast a db field as a complex (adapted) db field!");
        }
        return (Field.FieldWithAdapter<MO, C, T, F>) fieldWithAdapter;
    }

    public <C> Field.FieldWithAdapter<MO, C, T, F> asComplexField(Class<C> complexTypeClazz) throws ClassCastException {
        return asComplexField();
    }

    // Fields with types that need to be adapted are handled here
    // R is Complex result
    public static class FieldWithAdapter<M, C, T, F> extends Field<M, T, F> {
        public final Adapter<C, T> adapter;

        protected FieldWithAdapter(String name, DataType<T> type, Function<M, C> objFieldReader, Adapter<C, T> adapter, NuFallback<T, F> fallback) {
            super(name, type, objFieldReader, fallback);
            this.adapter = adapter;
        }

        @Override
        public T readField(M obj) {
            return adapter.adaptTo((C) objFieldReader.apply(obj));
        }
    }
}