package org.minerift.ether.database.sql.op.dml;

import org.jetbrains.annotations.Nullable;
import org.jooq.BatchBindStep;
import org.jooq.CloseableQuery;
import org.jooq.CloseableResultQuery;
import org.jooq.Record;
import org.minerift.ether.database.sql.SQLAccess;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.SQLUtils;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;
import org.minerift.ether.database.sql.op.dml.cache.RawQuery;

import java.util.Collection;

public class DMLSelectAll extends DMLOp {

    public DMLSelectAll(SQLDatabase db) {
        super(db);
    }

    @Override
    protected RawQuery newQueryForCache(Model<?, ?> model) {
        String sql = db.dsl().select().from(model.asJooqTable()).getSQL();
        return new RawQuery(sql, SQLUtils.EMPTY_BIND_VALS);
    }

    public <M, K> CloseableResultQuery<Record> getJooqQuery(SQLAccess access, Model<M, K> model) {
        return getJooqQuery(access, model, (NamedBindValues<?>) null);
    }

    // Bind vals can be null (no need for them)
    @Override
    public <M, K> CloseableResultQuery<Record> getJooqQuery(SQLAccess access, Model<M, K> model, @Nullable NamedBindValues<?> namedBindVals) {
        RawQuery rawQuery = queryFor(model);
        CloseableResultQuery<Record> query = access.dsl().resultQuery(rawQuery.getSql()).keepStatement(false);
        // No need to bind any values here
        return query;
    }

    // obj can be null (no need for obj)
    @Override
    public <M, K> CloseableResultQuery<Record> getJooqQuery(SQLAccess access, Model<M, K> model, @Nullable M obj) {
        return getJooqQuery(access, model);
    }

    @Override
    public <M, K> BatchBindStep getJooqBatch(SQLAccess access, Model<M, K> model, Collection<M> objs) {
        throw new UnsupportedOperationException("Unable to batch DML select queries!");
    }
}
