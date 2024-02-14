package org.minerift.ether.database.sql.op.dml;

import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.dml.cache.RawQuery;

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

        return new RawQuery(sql, keyEmptyField.keySet());
    }
}
