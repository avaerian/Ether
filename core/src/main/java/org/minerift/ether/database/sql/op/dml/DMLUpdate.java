package org.minerift.ether.database.sql.op.dml;

import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.dml.cache.RawQuery;

import static org.jooq.impl.DSL.condition;

public class DMLUpdate extends DMLOp {

    public DMLUpdate(SQLDatabase db) {
        super(db);
    }

    @Override
    protected RawQuery newQueryForCache(Model<?, ?> model) {
        var keyBindVals = model.getEmptyBindValuesUnchecked(model.getPrimaryKey());
        var fieldsNoKeyVals = model.getEmptyBindValuesUnchecked(model.getFieldsNoKey());
        String sql = db.dsl().update(model.asJooqTable())
                .set(fieldsNoKeyVals)
                .where(condition(keyBindVals))
                .getSQL();
        return new RawQuery(sql, fieldsNoKeyVals.keySet(), keyBindVals.keySet());
    }
}
