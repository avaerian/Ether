package org.minerift.ether.database.sql.operations.dml.query;

import org.jooq.CloseableQuery;
import org.minerift.ether.database.sql.model.Model;

public abstract class DMLQueryBase<T extends DMLQueryBase<T, Q>, Q extends CloseableQuery> {

    protected final Q query;
    protected final String[] bindOrder;

    public DMLQueryBase(Q query, String[] bindOrder) {
        this.query = query;
        this.bindOrder = bindOrder;
    }

    public <M> T bind(Model<M> model, M obj) {
        var namedBindVals = model.dumpNamedBindValues(obj);
        for(int i = 0; i < bindOrder.length; i++) {
            query.bind(i + 1, namedBindVals.get(bindOrder[i]));
        }
        return (T) this;
    }

    public int execute() {
        return query.execute();
    }

    public Q getQuery() {
        return query;
    }

    public String[] getBindOrder() {
        return bindOrder;
    }

}
