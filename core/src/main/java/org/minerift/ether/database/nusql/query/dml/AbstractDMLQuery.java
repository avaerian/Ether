package org.minerift.ether.database.nusql.query.dml;

import org.minerift.ether.database.Model;

import java.util.HashMap;
import java.util.Map;

public abstract class AbstractDMLQuery {

    public static class Entry {
        protected String sql;
    }

    protected final Map<Class<? extends Model<?,?>>, Entry> queries;

    public AbstractDMLQuery() {
        this.queries = new HashMap<>();
    }

    public abstract <MO> String getQuery(Model<MO, ?> model);

    //public abstract <MO> String getQueryCached(Model<MO, ?> model);
}
