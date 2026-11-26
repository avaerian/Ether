package org.minerift.ether.database.nusql.query.cond;

import org.jetbrains.annotations.NotNull;

public class Or extends LogicalCondition {

    public Or(@NotNull Condition condition) {
        super(condition);
    }

    @Override
    public CharSequence getSQLPart() {
        return "OR";
    }

    @Override
    public Type type() {
        return Type.OR;
    }
}
