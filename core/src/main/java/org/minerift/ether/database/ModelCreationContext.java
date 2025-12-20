package org.minerift.ether.database;

import com.google.common.collect.ImmutableMap;
import org.minerift.ether.database.sql.fallback.Fallback;
import org.minerift.ether.database.sql.adapters.Adapter;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import static org.minerift.ether.database.sql.SQLUtils.getPossibleFallback;

public class ModelCreationContext<MO> implements AutoCloseable {

    private DatabaseCreationContext dbCtx;
    private Model<MO, ?> model;
    private ImmutableMap.Builder<String, Field<MO, ?, ?>> fields; // TODO: review usage of this; must be a better way

    private String tableName;

    protected ModelCreationContext(DatabaseCreationContext dbCtx, Model<MO, ?> model) {
        this.dbCtx = dbCtx;
        this.model = model;
        this.fields = ImmutableMap.builder();
    }

    @Override
    public void close() {
        this.dbCtx = null;
        this.model = null;
        this.fields = null;
        this.tableName = null;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getTableName() {
        return tableName;
    }

    public ImmutableMap<String, Field<MO,?,?>> getFields() {
        return fields.build();
    }

    // MO = Modeled object
    public <T, F> Field<MO, T, F> createField(String name, DataType<T> type, Function<MO, T> objFieldReader) {
        Fallback<T, F> fallback = (Fallback<T, F>) getPossibleFallback(type, dbCtx);
        return createField(name, type, objFieldReader, fallback);
    }

    private <T, F> Field<MO, T, F> createField(String name, DataType<T> type, Function<MO, T> objFieldReader, Fallback<T, F> fallback) {
        Field<MO, T, F> field = new Field<>(name, type, model, objFieldReader, fallback);
        fields.put(field.getName(), field);
        return field;
    }

    public <C, T, F> Field<MO, T, F> createField(String name, DataType<T> type, Function<MO, C> objFieldReader, Adapter<C, T> adapter) {
        return createField(name, type, objFieldReader, adapter, (Fallback<T, F>) getPossibleFallback(type, dbCtx));
    }

    private <C, T, F> Field<MO, T, F> createField(String name, DataType<T> type, Function<MO, C> objFieldReader, Adapter<C, T> adapter, Fallback<T, F> fallback) {
        Field.FieldWithAdapter<MO, C, T, F> field = new Field.FieldWithAdapter<>(name, type, model, objFieldReader, adapter, fallback);
        fields.put(field.getName(), field);
        return field;
    }

    // The adapter, if any, is copied over as well, so no adapter needs to be inputted. The object field reader must return T or the complex type to get T
    public <C, T, F> Field<MO, T, F> createForeignField(Field<?, T, F> parentField, UnaryOperator<DataType<T>> addedFlags, Function<MO, ?> objFieldReader) {
        Field<MO, T, F> result;
        DataType<T> type = addedFlags == null ? parentField.requestedDataType : addedFlags.apply(parentField.requestedDataType);
        try {
            Field.FieldWithAdapter<?, C, T, F> complexField = parentField.asComplexField();
            result = createField(parentField.getName(), type, (Function<MO, C>)objFieldReader, complexField.adapter, complexField.fallback);
        } catch (ClassCastException ex) {
            result = createField(parentField.getName(), type, (Function<MO, T>)objFieldReader, parentField.fallback);
        }

        // NOTE: this is for resolving queries that may need to update multiple tables for an operation (i.e. delete op)
        Model parentModel = parentField.getOwner();
        parentModel.foreignFields.addDependent(parentField, model, result);

        return result;
    }

    public <T, F> Field<MO, T, F> createForeignField(Field<?, T, F> parentField, Function<MO, ?> objFieldReader) {
        return createForeignField(parentField, null, objFieldReader);
    }

    public <MODEL extends Model> MODEL getModel(Class<MODEL> modelClazz) {
        return dbCtx.getModel(modelClazz);
    }
}
