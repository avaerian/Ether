package org.minerift.ether.database;

import org.minerift.ether.database.nusql.op.bind.NamedBindValues;
import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.util.pair.Pair;

import java.util.*;

// obj represents modeled class
public abstract class Model<MO, PK> {
    protected String tableName;
    protected Fields<MO> fields;

    // A list of native fields that reference a foreign field
    protected List<Pair<Field<MO, ?, ?>, Field<?, ?, ?>>> foreignFieldRefs;

    public Model(DatabaseCreationContext dbCtx) {
        this.foreignFieldRefs = Collections.emptyList(); // if any foreign field refs are added, this list is updated in the ModelCreationContext
        var ctx = new ModelCreationContext<>(dbCtx, this);
        createModel(ctx);
        if(tableName.isEmpty()) {
            // TODO: throw exception
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

    // NOTE: for SQL stuffs
    public Field<?, ?, ?> getForeignFieldRef(Field<MO, ?, ?> field) {
        for(var entry : foreignFieldRefs) {
            if(entry.getFirst() == field) {
                return entry.getSecond();
            }
        }
        return null;
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

    protected abstract void createModel(ModelCreationContext<Model<MO, PK>, MO> ctx);

    public abstract IBuilder<MO> readAsBuilder(Record<MO> record);
    public abstract MO readRecord(Record<MO> record);

    public abstract Field<MO, PK, ?> getPrimaryKey();

}
