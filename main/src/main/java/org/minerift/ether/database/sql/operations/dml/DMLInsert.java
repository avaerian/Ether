package org.minerift.ether.database.sql.operations.dml;

import org.jooq.CloseableQuery;
import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.model.Fields;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.Batchable;
import org.minerift.ether.database.sql.operations.dml.cache.PerModelBindQueryCache;
import org.minerift.ether.database.sql.operations.dml.cache.RawBindQuery;
import org.minerift.ether.database.sql.operations.dml.query.DMLQuery;

public class DMLInsert extends PerModelDMLOp<RawBindQuery, DMLQuery> implements Batchable {

    public DMLInsert(SQLContext ctx) {
        super(ctx, new PerModelBindQueryCache());
    }

    @Override
    protected RawBindQuery getQueryForCache(Model<?> model) {
        Fields<?, ?> fields = model.getFields();
        String sql = ctx.dsl().insertInto(model.TABLE)
                .columns(fields.asSQLFields())
                .getSQL();
        return new RawBindQuery(sql, fields.getNames());
    }

    @Override
    public <M> DMLQuery getQuery(Model<M> model) {
        RawBindQuery rawBindQuery = queryCache.getQuery(model);
        CloseableQuery query = ctx.dsl().query(rawBindQuery.getSQL()).keepStatement(false);
        return new DMLQuery(query, rawBindQuery.getBindOrder());
    }

    @Override
    public <M> DMLModelBatch<M> getBatch(Model<M> model) {
        var query = getQuery(model);
        return new DMLModelBatch<>(ctx, model, query.getQuery(), query.getBindOrder());
    }
}
