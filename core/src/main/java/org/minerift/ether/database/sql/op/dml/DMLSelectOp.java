package org.minerift.ether.database.sql.op.dml;

import org.jooq.*;
import org.jooq.Record;
import org.minerift.ether.database.sql.SQLDatabaseCreationContext;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.sql.SQLAccess;
import org.minerift.ether.database.sql.SQLUtils;
import org.minerift.ether.database.sql.op.bind.NamedBindValues;

import java.util.Collection;

public abstract class DMLSelectOp extends DMLOp {
    public DMLSelectOp(SQLDatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected <OBJ, PK> ResultQuery<Record> prepareQuery(SQLAccess access, Model<OBJ, PK> model) {
        RawModelQuery rawQuery = getModelQuery(model);
        return access.dsl().resultQuery(rawQuery.getSql(), rawQuery.getNullValuesFromBindOrder());
    }

    @Override
    public <OBJ, PK> ResultQuery<Record> createExecutableQuery(SQLAccess access, Model<OBJ, PK> model, NamedBindValues<?> namedBindValues) {
        ResultQuery<Record> query = prepareQuery(access, model);
        SQLUtils.bind(query, model, namedBindValues, getModelQuery(model).getBindOrder());
        return query;
    }

    @Override
    public <OBJ, PK> ResultQuery<Record> createExecutableQuery(SQLAccess access, Model<OBJ, PK> model, OBJ obj) {
        return createExecutableQuery(access, model, model.dumpNamedBindValues(obj));
    }

    @Deprecated
    @Override
    public final <OBJ, PK> Batch createExecutableBatch(SQLAccess access, Model<OBJ, PK> model, Collection<OBJ> objs) {
        throw new UnsupportedOperationException("Unable to batch DML select queries");
    }
}
