package org.minerift.ether.database.sql.op.dml;

import org.minerift.ether.database.sql.SQLDatabaseCreationContext;
import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;

import static org.jooq.impl.DSL.condition;
import static org.minerift.ether.database.sql.SQLUtils.asJooqTable;
import static org.minerift.ether.database.sql.SQLUtils.getNullBindValue;

public class DMLSelectById extends DMLSelectOp {
    public DMLSelectById(SQLDatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected <OBJ, PK> RawModelQuery createQueryForModel(SQLDatabaseCreationContext ctx, Model<OBJ, PK> model) {
        Field<OBJ, PK, ?> primaryKey = model.getPrimaryKey();
        String sql = ctx.dsl().select()
                .from(asJooqTable(model))
                .where(condition(getNullBindValue(primaryKey)))
                .getSQL();
        return new RawModelQuery(sql, new String[] { primaryKey.getName() });
    }
}
