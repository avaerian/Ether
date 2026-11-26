package org.minerift.ether.database.nusql.query.cond;

import org.jetbrains.annotations.NotNull;

public class And extends LogicalCondition {

    //TODO: switch public to protected
    public And(@NotNull Condition condition) {
        super(condition);
    }

    @Override
    public CharSequence getSQLPart() {
        return "AND";
    }

    @Override
    public Type type() {
        return Type.AND;
    }
}
