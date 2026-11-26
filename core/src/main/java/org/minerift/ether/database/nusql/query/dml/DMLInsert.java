package org.minerift.ether.database.nusql.query.dml;

import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;

import java.util.Iterator;

// Builder for a SQL insert query
// TODO: consider different locales in future implementation
// https://www.jooq.org/doc/latest/manual/sql-building/dsl-context/custom-settings/settings-name-style/
public class DMLInsert extends AbstractDMLQuery {

    // INSERT INTO {table} ({columns}) VALUES ({values})

    public static <MO> String insertQuery(Model<MO, ?> model) {
        StringBuilder str = new StringBuilder("INSERT INTO ")
                .append(model.getTableName()).append(" (");

        Iterator<Field<MO, ?>> it = model.getFields().iterator();
        while(it.hasNext()) {
            Field<MO,?> f = it.next();
            str.append(f.getName()); //TODO: transform based on dialect
            if(it.hasNext())
                str.append(", ");
        }

        str.append(") VALUES (");

        int fieldsN = model.getFields().size();
        for(int i = 0; i < fieldsN; i++) {
            str.append('?');
            if(i < fieldsN - 1)
                str.append(", ");
        }

        str.append(')');

        return str.toString();
    }

    @Override
    public <MO> String getQuery(Model<MO, ?> model) {
        return insertQuery(model);
    }
}
