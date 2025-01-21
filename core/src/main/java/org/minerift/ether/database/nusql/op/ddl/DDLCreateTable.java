package org.minerift.ether.database.nusql.op.ddl;

import org.jooq.Constraint;
import org.jooq.DSLContext;
import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.NuSQLAccess;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jooq.impl.DSL.foreignKey;
import static org.jooq.impl.DSL.name;
import static org.minerift.ether.database.nusql.SQLUtils.*;

public class DDLCreateTable {

    public static <MO, PK> void createTableFromModel(NuSQLAccess access, Model<MO, PK> model) {

        List<Constraint> foreignKeys = Collections.emptyList();
        if(true) { // for debugging
            foreignKeys = new ArrayList<>();
            for(Field<MO, ?, ?> field : model.getFields()) {
                Field<?, ?, ?> ref = model.getForeignFieldRef(field);
                if(ref != null) {
                    Model<?, ?> foreignModel = access.db().getModel(ref.getOwner());
                    foreignKeys.add(foreignKey(field.getName()).references(name(foreignModel.getTableName()), name(ref.getName())));
                }
            }
        }

        final DSLContext dsl = access.dsl();
        dsl.createTable(asJooqTable(model))
                .columns(asJooqFields(model.getFields()))
                .primaryKey(asJooqField(model.getPrimaryKey()))
                .constraints(foreignKeys)
                .execute();
    }

}
