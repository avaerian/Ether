package org.minerift.ether.database.sql.op.dml;

import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.model.Fields;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.dml.cache.RawQuery;

public class DMLInsert extends DMLOp {


    public DMLInsert(SQLDatabase db) {
        super(db);
    }

    @Override
    protected RawQuery newQueryForCache(Model<?, ?> model) {
        Fields<?, ?> fields = model.getFields();
        String sql = db.dsl().insertInto(model.asJooqTable())
                .columns(fields.asSQLFields())
                .getSQL();
        return new RawQuery(sql, fields.getNames());
    }
}
