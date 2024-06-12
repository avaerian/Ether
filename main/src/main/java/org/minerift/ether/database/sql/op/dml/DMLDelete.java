package org.minerift.ether.database.sql.op.dml;

import org.jooq.BatchBindStep;
import org.jooq.CloseableQuery;
import org.minerift.ether.database.sql.SQLAccess;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.SQLUtils;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;
import org.minerift.ether.database.sql.op.dml.cache.RawQuery;

import java.util.Collection;
import java.util.Map;

import static org.jooq.impl.DSL.condition;

public class DMLDelete extends DMLOp {

    public DMLDelete(SQLDatabase db) {
        super(db);
    }

    @Override
    protected RawQuery newQueryForCache(Model<?, ?> model) {
        var keyEmptyField = model.getEmptyBindValuesUnchecked(model.getPrimaryKey());
        String sql = db.dsl().delete(model.asJooqTable())
                .where(condition(keyEmptyField))
                .getSQL();

        return new RawQuery(sql, new String[]{ model.getPrimaryKey().getName() });
    }

    // TODO: This is a little hardcoded; update API later to improve?
    public <M, K> BatchBindStep getJooqBatchIds(SQLAccess access, Model<M, K> model, Collection<K> ids) {
        RawQuery rawQuery = queryFor(model);
        CloseableQuery query = access.dsl().query(rawQuery.getSql(), rawQuery.getEmptyBindOrder()).keepStatement(false);
        BatchBindStep batch = access.dsl().batch(query);
        for(K id : ids) {
            batch.bind(NamedBindValues.of(model.getPrimaryKey(), id).getValuesFromBindOrder(model, rawQuery.getBindOrder()));
        }

        return batch;
    }
}
