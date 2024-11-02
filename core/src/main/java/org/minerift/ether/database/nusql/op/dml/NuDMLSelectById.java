package org.minerift.ether.database.nusql.op.dml;

import org.minerift.ether.database.nusql.SQLDatabaseCreationContext;
import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.SQLUtils;

import static org.jooq.impl.DSL.condition;
import static org.minerift.ether.database.nusql.SQLUtils.asJooqTable;
import static org.minerift.ether.database.nusql.SQLUtils.getNullBindValue;

public class NuDMLSelectById extends NuDMLSelectOp {
    public NuDMLSelectById(SQLDatabaseCreationContext dbCtx) {
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
