package org.minerift.ether.database.sql.operations.dml.cache;

import org.minerift.ether.database.sql.model.Model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class PerModelQueryCache<Q> extends QueryCache {

    private Map<Class<? extends Model>, Q> queryCache;

    public PerModelQueryCache() {
        this.queryCache = Collections.emptyMap();
    }

    public void cacheQueries(Collection<Model<?, ?>> tables, Function<Model<?, ?>, Q> cacheQueryFunc) {
        this.queryCache = new HashMap<>(tables.size());
        tables.forEach(model -> queryCache.put(model.getClass(), cacheQueryFunc.apply(model)));
    }

    public Q getQuery(Model<?, ?> model) {
        return queryCache.get(model.getClass());
    }
}
