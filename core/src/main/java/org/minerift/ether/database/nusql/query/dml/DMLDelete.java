package org.minerift.ether.database.nusql.query.dml;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.query.cond.Condition;

import static org.minerift.ether.database.nusql.query.cond.Condition.eq;

public class DMLDelete {

    public static <MO> String deleteQuery(Model<MO, ?> model, @NotNull Condition condition) {
        return "DELETE FROM " + model.getTableName() + " WHERE " + condition;
    }

    public static <MO> String deleteQuery(Model<MO, ?> model) {
        return deleteQuery(model, eq(model.getPrimaryKey()));
    }

}
