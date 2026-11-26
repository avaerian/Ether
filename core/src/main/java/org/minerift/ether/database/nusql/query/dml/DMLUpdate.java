package org.minerift.ether.database.nusql.query.dml;

import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.query.cond.Condition;

import javax.annotation.Nullable;
import java.util.Iterator;

import static org.minerift.ether.database.nusql.query.cond.Condition.*;

public class DMLUpdate extends AbstractDMLQuery {

    //UPDATE {table} SET {assignments} WHERE {condition}

    public static <MO> String updateQuery(Model<MO, ?> model, @Nullable Condition condition) {
        StringBuilder str = new StringBuilder("UPDATE ").append(model.getTableName()).append(" SET ");

        Iterator<Field<MO, ?>> it = model.getFields().exclude(model.getPrimaryKey()).iterator();
        while(it.hasNext()) {
            Field<MO, ?> field = it.next();
            str.append(field.getName()).append(" = ?");
            if(it.hasNext())
                str.append(", ");
        }

        if(condition != null)
            str.append(" WHERE ").append(condition);

        return str.toString();
    }

    public static <MO> String updateQuery(Model<MO, ?> model) {
        return updateQuery(model, eq(model.getPrimaryKey()));
    }

    @Override
    public <MO> String getQuery(Model<MO, ?> model) {
        return updateQuery(model);
    }
}
