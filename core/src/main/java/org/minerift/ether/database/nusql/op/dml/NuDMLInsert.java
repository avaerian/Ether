package org.minerift.ether.database.nusql.op.dml;

import org.minerift.ether.database.Fields;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.SQLDatabaseCreationContext;

import static org.minerift.ether.database.nusql.SQLUtils.asJooqFields;
import static org.minerift.ether.database.nusql.SQLUtils.asJooqTable;

public class NuDMLInsert extends NuDMLOp {
    public NuDMLInsert(SQLDatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected <MO, PK> RawModelQuery createQueryForModel(SQLDatabaseCreationContext ctx, Model<MO, PK> model) {
        Fields<MO> fields = model.getFields();
        String sql = ctx.dsl().insertInto(asJooqTable(model))
                .columns(asJooqFields(fields))
                .values(new Object[fields.size()])
                .getSQL();
        return new RawModelQuery(sql, fields.getNames());
    }
}
