package org.minerift.ether.database;

import com.google.common.collect.ImmutableMap;
import org.minerift.ether.database.nusql.fallback.NuFallback;
import org.minerift.ether.database.nusql.adapters.Adapter;
import org.minerift.ether.util.pair.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static org.minerift.ether.database.nusql.SQLUtils.getPossibleFallback;

public class ModelCreationContext<M extends Model<MO, ?>, MO> {

    private final DatabaseCreationContext dbCtx;
    private final M model;
    private final ImmutableMap.Builder<String, Field<MO, ?, ?>> fields;

    protected ModelCreationContext(DatabaseCreationContext dbCtx, M model) {
        this.dbCtx = dbCtx;
        this.model = model;
        this.fields = ImmutableMap.builder();
    }

    public void setTableName(String tableName) {
        model.tableName = tableName;
    }

    public ImmutableMap<String, Field<MO,?,?>> getFields() {
        return fields.build();
    }

    // MO = Modeled object
    public <T, F> Field<MO, T, F> createField(String name, DataType<T> type, Function<MO, T> objFieldReader) {
        NuFallback<T, F> fallback = (NuFallback<T, F>) getPossibleFallback(type, dbCtx);
        return createField(name, type, objFieldReader, fallback);
    }

    private <T, F> Field<MO, T, F> createField(String name, DataType<T> type, Function<MO, T> objFieldReader, NuFallback<T, F> fallback) {
        Field<MO, T, F> field = new Field<>(name, type, objFieldReader, fallback);
        fields.put(field.getName(), field);
        return field;
    }

    public <C, T, F> Field<MO, T, F> createField(String name, DataType<T> type, Function<MO, C> objFieldReader, Adapter<C, T> adapter) {
        return createField(name, type, objFieldReader, adapter, (NuFallback<T, F>) getPossibleFallback(type, dbCtx));
    }

    private <C, T, F> Field<MO, T, F> createField(String name, DataType<T> type, Function<MO, C> objFieldReader, Adapter<C, T> adapter, NuFallback<T, F> fallback) {
        Field.FieldWithAdapter<MO, C, T, F> field = new Field.FieldWithAdapter<>(name, type, objFieldReader, adapter, fallback);
        fields.put(field.getName(), field);
        return field;
    }

    // The adapter, if any, is copied over as well, so no adapter needs to be inputted. The object field reader must return T or the complex type to get T
    public <C, T, F> Field<MO, T, F> createForeignField(Field<?, T, F> parentField, Function<DataType<T>, DataType<T>> addedFlags, Function<MO, ?> objFieldReader) {

        Field<MO, T, F> result;
        DataType<T> type = addedFlags == null ? parentField.requestedDataType : addedFlags.apply(parentField.requestedDataType);
        try {
            Field.FieldWithAdapter<?, C, T, F> complexField = parentField.asComplexField();
            result = createField(parentField.getName(), type, (Function<MO, C>)objFieldReader, complexField.adapter, complexField.fallback);
        } catch (ClassCastException ex) {
            result = createField(parentField.getName(), type, (Function<MO, T>)objFieldReader, parentField.fallback);
        }

        // NOTE: this is for resolving queries that may need to update multiple tables for an operation (i.e. delete op)
        addForeignFieldRef(result, parentField);

        return result;
    }

    public <C, T, F> Field<MO, T, F> createForeignField(Field<?, T, F> parentField, Function<MO, ?> objFieldReader) {
        return createForeignField(parentField, null, objFieldReader);
    }

    private void addForeignFieldRef(Field<MO, ?, ?> nativeField, Field<?, ?, ?> foreignField) {
        if(model.foreignFieldRefs == Collections.EMPTY_LIST) {
            model.foreignFieldRefs = new ArrayList<>(4); // not expecting too many foreign fields at all
        }
        model.foreignFieldRefs.add(new Pair<>(nativeField, foreignField));
    }

    public <MODEL extends Model> MODEL getModel(Class<MODEL> modelClazz) {
        return dbCtx.getModel(modelClazz);
    }

}
