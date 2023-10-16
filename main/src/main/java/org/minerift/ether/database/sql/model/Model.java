package org.minerift.ether.database.sql.model;

import org.jooq.DataType;
import org.jooq.Record;
import org.jooq.Table;
import org.minerift.ether.database.sql.adapters.Adapter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.jooq.impl.DSL.table;

// Represents a database object model/table
public abstract class Model<M> {

    // TODO: logic for saving/loading objects should be located in each model

    public final String TABLE_NAME;
    public final Table<Record> TABLE;

    private final Fields<M, ?> fields;

    protected Model(String table) {
        this.TABLE_NAME = table;
        this.TABLE = table(TABLE_NAME);

        this.fields = Fields.of(this, fields());
    }

    public abstract Field<M, ?>[] fields();

    public Fields<M, ?> getFields() {
        return fields;
    }

    public Fields<M, ?> getPrimaryKeys() {
        return fields.getPrimaryKeys();
    }

    protected <D> Field<M, D> createField(String name, DataType<D> type, Function<M, D> objFieldReader) {
        return new Field<>(name, type, objFieldReader);
    }

    protected <D, R> Field<M, D> createField(String name, DataType<D> type, Function<M, R> objFieldReader, Adapter<R, D> adapter) {
        return new Field.FieldWithAdapter<>(name, type, objFieldReader, adapter);
    }

    public Map<org.jooq.Field<?>, ?> dumpSQLValuesForObj(M obj) {
        final Field<M, ?>[] fields = fields();
        final Map<org.jooq.Field<?>, Object> map = new HashMap<>(fields.length);
        for(Field<M, ?> field : fields) {
            map.put(field.getSQLField(), field.read(obj));
        }
        return map;
    }
}
