package org.minerift.ether.database.nusql.op.dml;

import org.jooq.Batch;
import org.jooq.BatchBindStep;
import org.jooq.Query;
import org.minerift.ether.database.nusql.SQLDatabaseCreationContext;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.NuSQLAccess;
import org.minerift.ether.database.nusql.SQLUtils;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class NuDMLOp {

    protected Map<Class<? extends Model>, RawModelQuery> queryCache;

    public NuDMLOp(SQLDatabaseCreationContext dbCtx) {
        this.queryCache = new HashMap<>(dbCtx.getModels().size());
        dbCtx.getModels().forEach(model -> queryCache.put(model.getClass(), createQueryForModel(dbCtx, model)));
    }

    protected abstract <OBJ, PK> RawModelQuery createQueryForModel(SQLDatabaseCreationContext ctx, Model<OBJ, PK> model);

    public RawModelQuery getModelQuery(Model<?, ?> model) {
        return getModelQuery(model.getClass());
    }

    public RawModelQuery getModelQuery(Class<? extends Model> modelClazz) {
        return queryCache.get(modelClazz);
    }

    protected <OBJ, PK> Query prepareQuery(NuSQLAccess access, Model<OBJ, PK> model) {
        RawModelQuery rawQuery = getModelQuery(model);
        return access.dsl().query(rawQuery.getSql(), rawQuery.getNullValuesFromBindOrder());
    }

    protected <OBJ, PK> BatchBindStep prepareBatch(NuSQLAccess access, Model<OBJ, PK> model) {
        return access.dsl().batch(prepareQuery(access, model));
    }

    public <OBJ, PK> Query createExecutableQuery(NuSQLAccess access, Model<OBJ, PK> model, NamedBindValues<?> namedBindValues) {
        Query query = prepareQuery(access, model);
        SQLUtils.bind(query, model, namedBindValues, getModelQuery(model).getBindOrder());
        return query;
    }

    public <OBJ, PK> Query createExecutableQuery(NuSQLAccess access, Model<OBJ, PK> model, OBJ obj) {
        return createExecutableQuery(access, model, model.dumpNamedBindValues(obj));
    }

    public <OBJ, PK> Batch createExecutableBatch(NuSQLAccess access, Model<OBJ, PK> model, Collection<OBJ> objs) {
        BatchBindStep batch = prepareBatch(access, model);
        SQLUtils.bind(batch, model, objs, getModelQuery(model).getBindOrder());
        return batch;
    }

}
