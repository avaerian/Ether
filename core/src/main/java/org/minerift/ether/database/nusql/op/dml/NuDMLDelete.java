package org.minerift.ether.database.nusql.op.dml;

import org.jooq.Batch;
import org.jooq.BatchBindStep;
import org.minerift.ether.database.Field;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.*;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;

import java.util.Collection;

import static org.jooq.impl.DSL.condition;
import static org.minerift.ether.database.nusql.SQLUtils.asJooqTable;
import static org.minerift.ether.database.nusql.SQLUtils.getNullBindValue;

public class NuDMLDelete extends NuDMLOp {
    public NuDMLDelete(SQLDatabaseCreationContext dbCtx) {
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

    public <OBJ, PK> Batch createExecutableBatchIds(NuSQLAccess access, Model<OBJ, PK> model, Collection<PK> ids) {
        String[] bindOrder = getModelQuery(model).getBindOrder();
        BatchBindStep batch = prepareBatch(access, model);
        for(PK id : ids) {
            batch.bind(NamedBindValues.of(model.getPrimaryKey(), id).getValuesFromBindOrder(model, bindOrder));
        }
        return batch;
    }
}
