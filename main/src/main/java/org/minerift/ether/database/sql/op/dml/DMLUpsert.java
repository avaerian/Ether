package org.minerift.ether.database.sql.op.dml;

import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.dml.cache.RawQuery;

import static org.jooq.impl.DSL.condition;

public class DMLUpsert extends DMLOp {

    public DMLUpsert(SQLDatabase db) {
        super(db);
    }

    @Override
    protected RawQuery newQueryForCache(Model<?, ?> model) {

        var allEmptyFields  = model.getEmptyBindValues();
        var partEmptyFields = model.getEmptyBindValuesUnchecked(model.getFieldsNoKey());

        return switch (db.getDialect()) {
            case MYSQL              -> {
                String sql = db.dsl().insertInto(model.asJooqTable())
                        .set(allEmptyFields)
                        .onDuplicateKeyUpdate()
                        .set(partEmptyFields)
                        .getSQL();
                yield new RawQuery(sql, allEmptyFields.keySet(), partEmptyFields.keySet());
            }

            case SQLITE, POSTGRES   -> {
                String sql = db.dsl().insertInto(model.asJooqTable())
                        .set(allEmptyFields)
                        .onConflict(model.getPrimaryKey().asJooqField())
                        .doUpdate()
                        .set(partEmptyFields)
                        .getSQL();
                yield new RawQuery(sql, allEmptyFields.keySet(), partEmptyFields.keySet());
            }

            case H2                 -> {
                var keyEmptyField = model.getEmptyBindValuesUnchecked(model.getPrimaryKey());
                String sql = db.dsl().mergeInto(model.asJooqTable())
                        .using(db.dsl().selectOne())
                        .on(condition(keyEmptyField))
                        .whenMatchedThenUpdate()
                        .set(partEmptyFields)
                        .whenNotMatchedThenInsert()
                        .set(allEmptyFields)
                        .getSQL();
                yield new RawQuery(sql, keyEmptyField.keySet(), partEmptyFields.keySet(), allEmptyFields.keySet());
            }
        };
    }
}
