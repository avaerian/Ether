package org.minerift.ether.database;

import org.jooq.Record;
import org.minerift.ether.database.nusql.NuSQLResult;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;
import org.minerift.ether.util.IBuilder;
import org.minerift.ether.util.pair.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// obj represents modeled class
public abstract class Model<MO, PK> {
    protected String tableName;
    protected Fields<MO> fields;

    // For each field, get a list of all the classes that use it as a foreign field
    protected List<Pair<Field<MO, ?, ?>, List<Class<? extends Model>>>> foreignFieldDependents;

    public Model(DatabaseCreationContext dbCtx) {
        this.foreignFieldDependents = new ArrayList<>(4); // not expecting too many foreign fields at all
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
    public List<Class<? extends Model>> getForeignFieldDependencies(Field<MO, ?, ?> foreignField) {
        for(var entry : foreignFieldDependents) {
            if(entry.getFirst() == foreignField) {
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

    public abstract IBuilder<MO> readAsBuilder(NuSQLResult<MO> result, Record record);
    public abstract MO readRecord(NuSQLResult<MO> result, Record record);

    public abstract Field<MO, PK, ?> getPrimaryKey();

}
