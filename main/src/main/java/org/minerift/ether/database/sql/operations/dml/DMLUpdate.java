package org.minerift.ether.database.sql.operations.dml;

import org.jooq.CloseableQuery;
import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.dml.cache.RawBindQuery;
import org.minerift.ether.database.sql.operations.dml.cache.PerModelBindQueryCache;
import org.minerift.ether.database.sql.operations.dml.query.DMLQuery;

import static org.jooq.impl.DSL.condition;

public class DMLUpdate extends PerModelDMLOp<RawBindQuery, DMLQuery> implements Batchable {

    public DMLUpdate(SQLContext ctx) {
        super(ctx, new PerModelBindQueryCache());
    }

    @Override
    protected RawBindQuery getQueryForCache(Model<?> model) {
        var keyBindVals = model.getEmptyBindValuesUnchecked(model.getPrimaryKey());
        var allBindVals = model.getEmptyBindValues();
        String sql = ctx.dsl().update(model.TABLE)
                .set(allBindVals)
                .where(condition(keyBindVals))
                .getSQL();
        return new RawBindQuery(sql, allBindVals.keySet(), keyBindVals.keySet());
    }

    @Override
    public <M> DMLQuery queryFor(Model<M> model) {
        RawBindQuery rawBindQuery = queryCache.getQuery(model);
        CloseableQuery query = ctx.dsl().query(rawBindQuery.getSQL()).keepStatement(false);
        return new DMLQuery(query, rawBindQuery.getBindOrder());
    }


    @Override
    public <M> DMLModelBatch<M> batchFor(Model<M> model) {
        var query = queryFor(model);
        return new DMLModelBatch<>(ctx, model, query.getQuery(), query.getBindOrder());
    }
}