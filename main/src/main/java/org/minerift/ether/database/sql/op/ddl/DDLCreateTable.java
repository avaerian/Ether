package org.minerift.ether.database.sql.op.ddl;

import org.jooq.DSLContext;
import org.minerift.ether.database.sql.SQLAccess;
import org.minerift.ether.database.sql.model.Model;

public class DDLCreateTable {

    public static <M, K> void createTableFromModel(SQLAccess access, Model<M, K> model) {
        final DSLContext dsl = access.dsl();
        dsl.createTable(model.asJooqTable())
                .columns(model.getFields().asJooqFields())
                .primaryKey(model.getPrimaryKey().asJooqField())
                .execute();
    }

}
