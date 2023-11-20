package org.minerift.ether.database.sql.operations.dml;

import org.jooq.CloseableQuery;
import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.dml.cache.PerModelQueryCache;
import org.minerift.ether.database.sql.operations.dml.query.DMLQueryBase;

// TODO: refactor PerModelDMLOp into DMLOp (every DML op will be handled per-model)
// Represents a DML operation that has per-model bind queries
// QCT - query cache type (String or BindQuery)
// Q - query type (CloseableQuery or CloseableResultQuery<Record>)
public abstract class PerModelDMLOp<QCT, Q extends DMLQueryBase<Q, ? extends CloseableQuery>> extends DMLOp<Q> {

    protected final PerModelQueryCache<QCT> queryCache;

    public PerModelDMLOp(SQLContext ctx, PerModelQueryCache<QCT> queryCache) {
        super(ctx);

        // Init cache
        this.queryCache = queryCache;
        queryCache.cacheQueries(ctx.getTables(), this::getQueryForCache);
    }

    protected abstract QCT getQueryForCache(Model<?> model);

    public abstract <M> Q queryFor(Model<M> model);
}