package org.minerift.ether.database.sql.operations.dml;

import org.jooq.CloseableResultQuery;
import org.jooq.Record;
import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.dml.cache.PerModelStaticQueryCache;
import org.minerift.ether.database.sql.operations.dml.query.DMLResultQuery;

import static org.minerift.ether.database.sql.SQLUtils.EMPTY_BIND_VALS;

public class DMLSelectAll extends PerModelDMLOp<String, DMLResultQuery> {

    public DMLSelectAll(SQLContext ctx) {
        super(ctx, new PerModelStaticQueryCache());
    }

    @Override
    protected String getQueryForCache(Model<?> model) {
        return ctx.dsl().select().from(model.TABLE).getSQL();
    }

    @Override
    public <M> DMLResultQuery queryFor(Model<M> model) {
        String sql = queryCache.getQuery(model);
        CloseableResultQuery<Record> query = ctx.dsl().resultQuery(sql, new Object[0]).keepStatement(false);
        return new DMLResultQuery(query, EMPTY_BIND_VALS);
    }
}