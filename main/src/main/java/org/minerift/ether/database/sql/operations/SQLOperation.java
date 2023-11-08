package org.minerift.ether.database.sql.operations;

import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.SQLDialect;

@Deprecated(forRemoval = true)
public abstract class SQLOperation {

    protected final SQLContext ctx;

    public SQLOperation(SQLContext ctx) {
        this.ctx = ctx;
    }

    public SQLContext getSQLContext() {
        return ctx;
    }

    public SQLDialect getDialect() {
        return ctx.getDialect();
    }

}
