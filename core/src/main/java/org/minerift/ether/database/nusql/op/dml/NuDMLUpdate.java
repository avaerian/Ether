package org.minerift.ether.database.nusql.op.dml;

import org.minerift.ether.database.Field;
import org.minerift.ether.database.Fields;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.*;

import static org.jooq.impl.DSL.condition;
import static org.minerift.ether.database.nusql.SQLUtils.*;

public class NuDMLUpdate extends NuDMLOp {
    public NuDMLUpdate(SQLDatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected <MO, PK> RawModelQuery createQueryForModel(SQLDatabaseCreationContext ctx, Model<MO, PK> model) {
        Field<MO, PK, ?> primaryKey = model.getPrimaryKey();
        Fields<MO> fieldsNoKey = model.getFields().exclude(primaryKey);
        String sql = ctx.dsl().update(asJooqTable(model))
                .set(getNullBindValues(fieldsNoKey))
                .where(condition(getNullBindValue(primaryKey)))
                .getSQL();
        return new RawModelQuery(sql, fieldsNoKey.getNames(), new String[] { primaryKey.getName() });
    }
}
