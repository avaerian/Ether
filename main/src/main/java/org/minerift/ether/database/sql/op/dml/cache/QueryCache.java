package org.minerift.ether.database.sql.op.dml.cache;

import org.minerift.ether.database.sql.model.Model;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class QueryCache {

    private Map<Class<? extends Model>, RawQuery> modelQueryCache;

    public QueryCache() {
        this.modelQueryCache = Collections.emptyMap();
    }

    public void cacheQueries(Collection<Model<?, ?>> tables, Function<Model<?, ?>, RawQuery> cacheQueryFunc) {
        this.modelQueryCache = new HashMap<>(tables.size());
        tables.forEach(model -> modelQueryCache.put(model.getClass(), cacheQueryFunc.apply(model)));
    }

    public RawQuery getQuery(Model<?, ?> model) {
        return modelQueryCache.get(model.getClass());
    }

}
