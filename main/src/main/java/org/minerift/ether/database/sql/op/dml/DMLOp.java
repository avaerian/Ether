package org.minerift.ether.database.sql.op.dml;

import org.jooq.BatchBindStep;
import org.jooq.CloseableQuery;
import org.minerift.ether.database.sql.SQLAccess;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.SQLUtils;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;
import org.minerift.ether.database.sql.op.dml.cache.RawQuery;
import org.minerift.ether.database.sql.op.dml.cache.QueryCache;

import java.util.Collection;

public abstract class DMLOp {

    protected final SQLDatabase db;
    protected final QueryCache queryCache;

    public DMLOp(SQLDatabase db) {
        this.db = db;
        this.queryCache = new QueryCache();
        queryCache.cacheQueries(db.getTables(), this::newQueryForCache);
    }

    protected abstract RawQuery newQueryForCache(Model<?, ?> model);

    public <M> RawQuery queryFor(Model<M, ?> model) {
        return queryCache.getQuery(model);
    }

    public <M, K> CloseableQuery getJooqQuery(SQLAccess access, Model<M, K> model, NamedBindValues<?> namedBindVals) {
        RawQuery rawQuery = queryFor(model);
        CloseableQuery query = access.dsl().query(rawQuery.getSql(), rawQuery.getEmptyBindOrder()).keepStatement(false);
        SQLUtils.bind(query, namedBindVals, rawQuery.getBindOrder());
        return query;
    }

    public <M, K> CloseableQuery getJooqQuery(SQLAccess access, Model<M, K> model, M obj) {
        return getJooqQuery(access, model, model.dumpNamedBindValues_New(obj));
    }

    public <M, K> BatchBindStep getJooqBatch(SQLAccess access, Model<M, K> model, Collection<M> objs) {
        RawQuery rawQuery = queryFor(model);
        CloseableQuery query = access.dsl().query(rawQuery.getSql(), rawQuery.getEmptyBindOrder()).keepStatement(false);
        BatchBindStep batch = SQLUtils.bindToBatch(access.dsl().batch(query), model, objs);
        return batch;
    }
}
