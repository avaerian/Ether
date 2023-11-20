package org.minerift.ether.database.sql.operations.dml;

import org.jooq.CloseableResultQuery;
import org.jooq.Record;
import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.dml.cache.PerModelBindQueryCache;
import org.minerift.ether.database.sql.operations.dml.cache.RawBindQuery;
import org.minerift.ether.database.sql.operations.dml.query.DMLResultQuery;

import static org.jooq.impl.DSL.condition;

public class DMLSelectObject extends PerModelDMLOp<RawBindQuery, DMLResultQuery> {

    public DMLSelectObject(SQLContext ctx) {
        super(ctx, new PerModelBindQueryCache());
    }

    @Override
    protected RawBindQuery getQueryForCache(Model<?> model) {
        var keyBindVals = model.getEmptyBindValuesUnchecked(model.getPrimaryKey());
        String sql = ctx.dsl().select()
                .from(model.TABLE)
                .where(condition(keyBindVals))
                .getSQL();
        System.out.println(sql);
        return new RawBindQuery(sql, keyBindVals.keySet());
    }

    @Override
    public <M> DMLResultQuery queryFor(Model<M> model) {
        RawBindQuery rawBindQuery = queryCache.getQuery(model);
        CloseableResultQuery<Record> query = ctx.dsl().resultQuery(rawBindQuery.getSQL(), new Object[rawBindQuery.getBindOrder().length]).keepStatement(false);
        return new DMLResultQuery(query, rawBindQuery.getBindOrder());
    }
}
