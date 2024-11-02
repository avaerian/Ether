package org.minerift.ether.database.nusql.op.dml;

import org.jooq.*;
import org.jooq.Record;
import org.minerift.ether.database.nusql.SQLDatabaseCreationContext;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.NuSQLAccess;
import org.minerift.ether.database.nusql.SQLUtils;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;

import java.util.Collection;

public abstract class NuDMLSelectOp extends NuDMLOp {
    public NuDMLSelectOp(SQLDatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected <OBJ, PK> ResultQuery<Record> prepareQuery(NuSQLAccess access, Model<OBJ, PK> model) {
        RawModelQuery rawQuery = getModelQuery(model);
        return access.dsl().resultQuery(rawQuery.getSql(), rawQuery.getNullValuesFromBindOrder());
    }

    @Override
    public <OBJ, PK> ResultQuery<Record> createExecutableQuery(NuSQLAccess access, Model<OBJ, PK> model, NamedBindValues<?> namedBindValues) {
        ResultQuery<Record> query = prepareQuery(access, model);
        SQLUtils.bind(query, model, namedBindValues, getModelQuery(model).getBindOrder());
        return query;
    }

    @Override
    public <OBJ, PK> ResultQuery<Record> createExecutableQuery(NuSQLAccess access, Model<OBJ, PK> model, OBJ obj) {
        return createExecutableQuery(access, model, model.dumpNamedBindValues(obj));
    }

    @Deprecated
    @Override
    public final <OBJ, PK> Batch createExecutableBatch(NuSQLAccess access, Model<OBJ, PK> model, Collection<OBJ> objs) {
        throw new UnsupportedOperationException("Unable to batch DML select queries");
    }
}
