package org.minerift.ether.database.sql.op.dml;

import org.minerift.ether.database.Fields;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.sql.SQLDatabaseCreationContext;

import static org.minerift.ether.database.sql.SQLUtils.asJooqFields;
import static org.minerift.ether.database.sql.SQLUtils.asJooqTable;

public class DMLInsert extends DMLOp {
    public DMLInsert(SQLDatabaseCreationContext dbCtx) {
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
