package org.minerift.ether.database.sql.operations.dml;

import org.jooq.CloseableQuery;
import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.operations.dml.query.DMLQueryBase;

public abstract class DMLOp<Q extends DMLQueryBase<Q, ? extends CloseableQuery>> {

    protected final SQLContext ctx;

    public DMLOp(SQLContext ctx) {
        this.ctx = ctx;
    }
}
