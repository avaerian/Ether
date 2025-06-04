package org.minerift.ether.database;

import org.minerift.ether.database.nusql.op.bind.NamedBindValues;
import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.util.pair.Pair;

import java.util.*;

// obj represents modeled class
public abstract class Model<MO, PK> {
    protected String tableName;
    protected Fields<MO> fields;
    protected ForeignFields<MO> foreignFields;

    public Model(DatabaseCreationContext dbCtx) {
        this.foreignFields = new ForeignFields<>(this);
        var ctx = new ModelCreationContext<>(dbCtx, this);
        createModel(ctx);
        if(tableName.isBlank()) {
            throw new IllegalStateException("Table name cannot be empty!");
        }
        this.fields = new Fields<>(this, ctx.getFields());
    }

    public String getTableName() {
        return tableName;
    }

    public Field<MO, ?, ?> getField(String name) {
        return fields.getField(name);
    }

    public Fields<MO> getFields() {
        return fields;
    }

    public ForeignFields<MO> getForeignFields() {
        return foreignFields;
    }

    // NOTE: for SQL stuffs
    public NamedBindValues<?> dumpNamedBindValues(MO obj) {
        Map<String, Object> bindVals = new HashMap<>(fields.size());
        for(Field<MO, ?, ?> field : fields) {
            bindVals.put(field.getName(), field.readAsSQLValue(obj));
        }
        return NamedBindValues.of(bindVals);
    }

    // NOTE: for SQL stuffs
    public Object[] dumpOrderedBindValues(MO obj, String ... bindOrder) {
        Object[] bindVals = new Object[bindOrder.length];
        NamedBindValues<?> namedBindValues = dumpNamedBindValues(obj);
        for(int i = 0; i < bindOrder.length; i++) {
            String column = bindOrder[i];
            bindVals[i] = getField(column).readJavaAsSQLValue(namedBindValues.getFieldValue(column));
        }
        return bindVals;
    }

    protected abstract void createModel(ModelCreationContext<MO> ctx);

    public abstract IBuilder<MO> readAsBuilder(Record<MO> record);
    public abstract MO readRecord(Record<MO> record);

    public abstract Field<MO, PK, ?> getPrimaryKey();

}
