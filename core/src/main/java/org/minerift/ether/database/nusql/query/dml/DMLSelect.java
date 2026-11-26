package org.minerift.ether.database.nusql.query.dml;

import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.query.cond.Condition;

import javax.annotation.Nullable;

public class DMLSelect {
    public static <MO> String selectQuery(Model<MO, ?> model, @Nullable Condition condition) {
        StringBuilder str = new StringBuilder("SELECT * FROM ").append(model.getTableName());
        if(condition != null) {
            condition = condition.root();
            str.append(" WHERE ").append(condition);
        }
        return str.toString();
    }
}
