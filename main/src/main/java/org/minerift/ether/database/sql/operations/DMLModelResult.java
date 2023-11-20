package org.minerift.ether.database.sql.operations;

import org.jooq.Record;
import org.jooq.Result;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;

public class DMLModelResult<M> {

    private final Model<M> model;
    private final Result<Record> result;

    public DMLModelResult(Model<M> model, Result<Record> result) {
        this.model = model;
        this.result = result;
    }

    public Result<Record> getSQLResult() {
        return result;
    }

    public <T> T getField(Field<M, T, ?> field) {
        // TODO
        return field.readSQLValue(result.get(0).get(field.getName()));
    }

}
