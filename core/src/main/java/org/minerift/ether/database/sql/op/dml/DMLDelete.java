package org.minerift.ether.database.sql.op.dml;

import org.jooq.Batch;
import org.jooq.BatchBindStep;
import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.sql.op.bind.NamedBindValues;
import org.minerift.ether.database.sql.SQLAccess;
import org.minerift.ether.database.sql.SQLDatabaseCreationContext;

import java.util.Collection;

import static org.jooq.impl.DSL.condition;
import static org.minerift.ether.database.sql.SQLUtils.asJooqTable;
import static org.minerift.ether.database.sql.SQLUtils.getNullBindValue;

public class DMLDelete extends DMLOp {
    public DMLDelete(SQLDatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected <OBJ, PK> RawModelQuery createQueryForModel(SQLDatabaseCreationContext ctx, Model<OBJ, PK> model) {
        Field<OBJ, PK, ?> primaryKey = model.getPrimaryKey();
        String sql = ctx.dsl().delete(asJooqTable(model))
                .where(condition(getNullBindValue(primaryKey)))
                .getSQL();
        return new RawModelQuery(sql, new String[] { primaryKey.getName() });
    }

    public <OBJ, PK> Batch createExecutableBatchIds(SQLAccess access, Model<OBJ, PK> model, Collection<PK> ids) {
        String[] bindOrder = getModelQuery(model).getBindOrder();
        BatchBindStep batch = prepareBatch(access, model);
        for(PK id : ids) {
            batch.bind(NamedBindValues.of(model.getPrimaryKey(), id).getValuesFromBindOrder(model, bindOrder));
        }
        return batch;
    }
}
