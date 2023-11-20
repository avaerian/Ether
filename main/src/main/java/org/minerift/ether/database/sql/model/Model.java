package org.minerift.ether.database.sql.model;

import com.google.common.annotations.Beta;
import org.jooq.CloseableQuery;
import org.jooq.DataType;
import org.jooq.Record;
import org.jooq.Table;
import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.adapters.Adapter;
import org.minerift.ether.database.sql.fallback.JsonFallback;
import org.minerift.ether.database.sql.operations.dml.bind.NamedBindValues;
import org.minerift.ether.util.reflect.Reflect;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.jooq.impl.DSL.table;
import static org.minerift.ether.database.sql.SQLUtils.isDataTypeSupported;

// Represents a database object model/table
public abstract class Model<M> {

    protected final SQLContext ctx;

    public final String TABLE_NAME;
    public final Table<Record> TABLE;

    private Fields<M, ?> fields;

    protected Model(String table, SQLContext ctx) {
        this.ctx = ctx;
        this.TABLE_NAME = table;
        this.TABLE = table(TABLE_NAME);
    }

    // Must be called after superconstructor
    protected void setupFields() {
        this.fields = Fields.of(this, fields());
    }

    public abstract Field<M, ?, ?>[] fields();

    public SQLContext getSQLContext() {
        return ctx;
    }

    public Fields<M, ?> getFields() {
        return fields;
    }

    public abstract Field<M, ?, ?> getPrimaryKey();

    public Fields<M, ?> getUniqueFields() {
        return fields.getUniqueFields();
    }

    public Fields<M, ?> getFieldsNoKey() {
        var reflectedObj = Reflect.of(this);
        Field<M, ?, ?>[] fields = reflectedObj.getFieldsFromRefs(fields())
                .filter(field -> !field.hasAnnotation(PrimaryKey.class))
                .readAllTyped(this, Field.class);
        return Fields.of(this, fields);
    }

    protected <D> Field<M, D, ?> createField(String name, DataType<D> type, Function<M, D> objFieldReader) {

        JsonFallback<D> fallback = isDataTypeSupported(type, ctx.getDialect())
                ? null // no fallback adapter needed for supported types
                : new JsonFallback<>(type.getType());

        return new Field<>(name, type, objFieldReader, fallback);
    }

    protected <D, R> Field<M, D, ?> createField(String name, DataType<D> type, Function<M, R> objFieldReader, Adapter<R, D> adapter) {

        JsonFallback<D> fallback = isDataTypeSupported(type, ctx.getDialect())
                ? null // no fallback adapter needed for supported types
                : new JsonFallback<>(type.getType());

        return new Field.FieldWithAdapter<>(name, type, objFieldReader, adapter, fallback);
    }

    @Deprecated(forRemoval = true)
    public Map<org.jooq.Field<?>, ?> dumpSQLValuesForObj(M obj) {
        return dumpSQLValuesForObj(obj, fields.getArray());
    }

    @Deprecated(forRemoval = true)
    public Map<org.jooq.Field<?>, ?> dumpSQLValuesForObj(M obj, Field<M, ?, ?>[] selectedFields) {
        final Map<org.jooq.Field<?>, Object> map = new HashMap<>(selectedFields.length);
        for(Field<M, ?, ?> field : selectedFields) {
            map.put(field.getSQLField(), field.readAsSQLValue(obj));
        }
        return map;
    }

    @Deprecated
    public void bindObjToQuery(CloseableQuery query, M obj, String[] bindOrder) {
        var namedBindVals = dumpNamedBindValues(obj);
        for(int i = 0; i < bindOrder.length; i++) {
            query.bind(i + 1, namedBindVals.get(bindOrder[i]));
        }
    }

    @Beta
    public NamedBindValues<?> dumpNamedBindValues_New(M obj) {
        if(fields.size() == 1) {
            Field<M, ?, ?> field = fields.getArray()[0];
            return NamedBindValues.of(field.getName(), field.readAsSQLValue(obj));
        } else {
            Map<String, Object> bindVals = new HashMap<>(fields.size());
            for(Field<M, ?, ?> field : fields) {
                bindVals.put(field.getName(), field.readAsSQLValue(obj));
            }
            return NamedBindValues.of(bindVals);
        }
    }

    // Returns empty bind values (field with null value) for all fields
    public Map<org.jooq.Field<?>, ?> getEmptyBindValues() {
        return getEmptyBindValues(fields);
    }

    // Returns empty bind values for a single field
    public Map<org.jooq.Field<?>, ?> getEmptyBindValues(Field<M, ?, ?> field) {
        return getEmptyBindValuesUnchecked(field);
    }

    // Returns empty bind values for selected fields
    public Map<org.jooq.Field<?>, ?> getEmptyBindValues(Fields<M, ?> selectedFields) {
        return getEmptyBindValuesUnchecked(selectedFields);
    }

    public Map<org.jooq.Field<?>, ?> getEmptyBindValuesUnchecked(Field<?, ?, ?> field) {
        Map<org.jooq.Field<?>, ?> bindVals = new HashMap<>(1);
        bindVals.put(field.getSQLField(), null);
        return bindVals;
        //return Map.of(field.getSQLField(), null);
    }

    // Doesn't check model type for Fields arg (regarding generics)
    public Map<org.jooq.Field<?>, ?> getEmptyBindValuesUnchecked(Fields<?, ?> selectedFields) {
        Map<org.jooq.Field<?>, ?> bindVals = new HashMap<>(selectedFields.size());
        selectedFields.forEach(field -> bindVals.put(field.getSQLField(), null));
        return bindVals;
    }

    public Map<String, Object> dumpNamedBindValues(M obj) {
        Map<String, Object> bindVals = new HashMap<>(fields.size());
        for(Field<M, ?, ?> field : fields) {
            bindVals.put(field.getSQLField().getName(), field.readAsSQLValue(obj));
        }
        return bindVals;
    }

    // TODO: deprecate
    public Object[] dumpBindValues(M obj, String ... bindOrder) {
        Object[] bindVals = new Object[bindOrder.length];
        var namedBindValues = dumpNamedBindValues(obj);
        for(int i = 0; i < bindOrder.length; i++) {
            bindVals[i] = namedBindValues.get(bindOrder[i]);
        }
        return bindVals;
    }
}