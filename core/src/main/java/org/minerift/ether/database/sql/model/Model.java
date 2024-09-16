package org.minerift.ether.database.sql.model;

import com.google.common.annotations.Beta;
import org.jooq.CloseableQuery;
import org.jooq.DataType;
import org.jooq.Record;
import org.jooq.Table;
import org.minerift.ether.Ether;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.SQLResult;
import org.minerift.ether.database.sql.adapters.Adapter;
import org.minerift.ether.database.sql.fallback.Fallback;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;
import org.minerift.ether.island.invites.IslandInvite;
import org.minerift.ether.island.invites.IslandInviteManager;
import org.minerift.ether.island.invites.IslandInvitesModel;
import org.minerift.ether.util.IBuilder;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.util.reflect.ReflectedFields;

import java.util.*;
import java.util.function.Function;

import static org.jooq.impl.DSL.name;
import static org.jooq.impl.DSL.table;
import static org.minerift.ether.database.sql.SQLUtils.getPossibleFallback;

// Represents a database object model/table
public abstract class Model<M, K> {

    protected final SQLDatabase db;

    public final String TABLE_NAME;

    private Fields<M, ?> fields;

    private final List<Field<M, ?, ?>> prepFields = new ArrayList<>();

    public Model(String tableName, SQLDatabase db) {
        this.db = db;
        this.TABLE_NAME = tableName;
    }

    // NOTE: No fields associated with this Jooq table object
    // This is purely for SQL formatting when building queries
    public Table<Record> asJooqTable() {
        return table(name(TABLE_NAME));
    }

    // Must be called after superconstructor
    protected final void registerFields() {
        this.fields = Fields.of(this, prepFields.toArray(Field[]::new));
    }

    // Reads the full SQL result to read in a more complete object
    public abstract M readResult(SQLResult<M> result, Record record);

    // Should return an incomplete prototype of an object from SQL result
    // Incomplete prototype can be modified after being read from result
    // Not all fields from SQL result may be read in at the same time, so those fields will be processed together (deferred)
    public abstract IBuilder<M> readAsBuilder(SQLResult<M> result, Record record);

    public SQLDatabase getDb() {
        return db;
    }

    public Fields<M, ?> getFields() {
        return fields;
    }

    public abstract Field<M, K, ?> getPrimaryKey();

    public Fields<M, ?> getUniqueFields() {
        return fields.getUniqueFields();
    }

    public Fields<M, ?> getFieldsNoKey() {
        Field<M, ?, ?>[] fieldsNoKey = Arrays.stream(fields.getArray()).filter(field -> field != getPrimaryKey()).toArray(Field[]::new);
        return Fields.of(this, fieldsNoKey);
    }

    protected <D> Field<M, D, ?> createField(String name, DataType<D> type, Function<M, D> objFieldReader) {
        /*
        JsonFallback<D> fallback = isDataTypeSupported(type, db.getDialect())
                ? null // no fallback adapter needed for supported types
                : new JsonFallback<>(type.getType());
        */

        Fallback<D, ?> fallback = getPossibleFallback(type, db.getDialect());
        Field<M, D, ?> field = new Field<>(name, type, objFieldReader, fallback);
        prepFields.add(field);
        return field;
    }

    protected <D, R> Field<M, D, ?> createField(String name, DataType<D> type, Function<M, R> objFieldReader, Adapter<R, D> adapter) {
        /*
        JsonFallback<D> fallback = isDataTypeSupported(type, db.getDialect())
                ? null // no fallback adapter needed for supported types
                : new JsonFallback<>(type.getType());
        */

        Fallback<D, ?> fallback = getPossibleFallback(type, db.getDialect());
        Field<M, D, ?> field = new Field.FieldWithAdapter<>(name, type, objFieldReader, adapter, fallback);
        prepFields.add(field);
        return field;
    }

    // NOTE: Ignores name case when finding field
    public Field<M, ?, ?> getField(String name) {
        // assumes all fields in model belong to model (no weird shit)
        Field<M, ?, ?>[] fields = Reflect.of(this).getFields().filter(field -> field.isType(Field.class)).readAllTyped(this, Field.class);
        for(Field<M, ?, ?> field : fields) {
            if(field.getName().equalsIgnoreCase(name)) {
                return field;
            }
        }
        Ether.getLogger().warning("Could not find field in " + TABLE_NAME + " by the name " + name);
        return null;
    }

    @Deprecated(forRemoval = true)
    public Map<org.jooq.Field<?>, ?> dumpSQLValuesForObj(M obj) {
        return dumpSQLValuesForObj(obj, fields.getArray());
    }

    @Deprecated(forRemoval = true)
    public Map<org.jooq.Field<?>, ?> dumpSQLValuesForObj(M obj, Field<M, ?, ?>[] selectedFields) {
        final Map<org.jooq.Field<?>, Object> map = new HashMap<>(selectedFields.length);
        for(Field<M, ?, ?> field : selectedFields) {
            map.put(field.asJooqField(), field.readAsSQLValue(obj));
        }
        return map;
    }

    @Deprecated(forRemoval = true)
    public void bindObjToQuery(CloseableQuery query, M obj, String[] bindOrder) {
        var namedBindVals = dumpNamedBindValues(obj);
        for(int i = 0; i < bindOrder.length; i++) {
            query.bind(i + 1, namedBindVals.get(bindOrder[i]));
        }
    }

    // TODO: review and shorten
    @Beta
    public NamedBindValues<?> dumpNamedBindValues_New(M obj) {
        Map<String, Object> bindVals = new HashMap<>(fields.size());
        for(Field<M, ?, ?> field : fields) {
            bindVals.put(field.getName(), field.readAsSQLValue(obj));
        }
        return NamedBindValues.of(bindVals);
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

    // TODO: review this to shorten if possible
    public Map<org.jooq.Field<?>, ?> getEmptyBindValuesUnchecked(Field<?, ?, ?> field) {
        Map<org.jooq.Field<?>, ?> bindVals = new HashMap<>(1);
        bindVals.put(field.asJooqField(), null);
        return bindVals;
        //return Map.of(field.getSQLField(), null);
    }

    // Doesn't check model type for Fields arg (no generics)
    public Map<org.jooq.Field<?>, ?> getEmptyBindValuesUnchecked(Fields<?, ?> selectedFields) {
        Map<org.jooq.Field<?>, ?> bindVals = new HashMap<>(selectedFields.size());
        selectedFields.forEach(field -> bindVals.put(field.asJooqField(), null));
        return bindVals;
    }

    public Map<String, Object> dumpNamedBindValues(M obj) {
        Map<String, Object> bindVals = new HashMap<>(fields.size());
        for(Field<M, ?, ?> field : fields) {
            bindVals.put(field.asJooqField().getName(), field.readAsSQLValue(obj));
        }
        return bindVals;
    }

    public Object[] dumpBindValues(M obj, String ... bindOrder) {
        Object[] bindVals = new Object[bindOrder.length];
        Map<String, Object> namedBindValues = dumpNamedBindValues(obj);
        for(int i = 0; i < bindOrder.length; i++) {
            String column = bindOrder[i];
            bindVals[i] = getField(column).readJavaAsSQLValue(namedBindValues.get(column));
        }
        return bindVals;
    }
}
