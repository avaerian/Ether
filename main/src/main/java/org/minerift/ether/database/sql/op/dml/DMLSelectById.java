package org.minerift.ether.database.sql.op.dml;

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

import static org.jooq.impl.DSL.condition;

public class DMLSelectById extends DMLOp {

    public DMLSelectById(SQLDatabase db) {
        super(db);
    }

    @Override
    protected RawQuery newQueryForCache(Model<?, ?> model) {
        var keyBindVals = model.getEmptyBindValuesUnchecked(model.getPrimaryKey());
        String sql = db.dsl().select()
                .from(model.asJooqTable())
                .where(condition(keyBindVals))
                .getSQL();
        return new RawQuery(sql, keyBindVals.keySet());
    }

    @Override
    public <M, K> CloseableResultQuery<Record> getJooqQuery(SQLAccess access, Model<M, K> model, NamedBindValues<?> namedBindVals) {
        RawQuery rawQuery = queryFor(model);
        CloseableResultQuery<Record> query = access.dsl().resultQuery(rawQuery.getSql(), rawQuery.getEmptyBindOrder()).keepStatement(false);
        SQLUtils.bind(query, namedBindVals, rawQuery.getBindOrder());
        return query;
    }

    @Override
    public <M, K> CloseableResultQuery<Record> getJooqQuery(SQLAccess access, Model<M, K> model, M obj) {
        return getJooqQuery(access, model, model.dumpNamedBindValues_New(obj));
    }

    @Override
    public <M, K> BatchBindStep getJooqBatch(SQLAccess access, Model<M, K> model, Collection<M> objs) {
        throw new UnsupportedOperationException("Unable to batch DML select queries!");
    }
}
