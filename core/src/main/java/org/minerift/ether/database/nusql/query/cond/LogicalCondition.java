package org.minerift.ether.database.nusql.query.cond;

import org.jetbrains.annotations.NotNull;

public abstract class LogicalCondition extends Condition {

    protected @NotNull final Condition right; //may be between parens if next condition exists

    protected LogicalCondition(@NotNull Condition right) {
        this.right = right;
    }

    public @NotNull Condition getRightSide() {
        return right;
    }

    @Override
    public boolean isLogical() {
        return true;
    }
}
