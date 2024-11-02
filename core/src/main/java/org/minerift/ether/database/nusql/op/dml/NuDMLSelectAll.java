package org.minerift.ether.database.nusql.op.dml;

import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.minerift.ether.database.nusql.SQLDatabaseCreationContext;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.NuSQLAccess;
import org.minerift.ether.database.nusql.SQLUtils;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;

import static org.minerift.ether.database.nusql.SQLUtils.EMPTY_BIND_VALS;
import static org.minerift.ether.database.nusql.SQLUtils.asJooqTable;

public class NuDMLSelectAll extends NuDMLSelectOp {
    public NuDMLSelectAll(SQLDatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected <OBJ, PK> RawModelQuery createQueryForModel(SQLDatabaseCreationContext ctx, Model<OBJ, PK> model) {
        String sql = ctx.dsl().select().from(asJooqTable(model)).getSQL();
        return new RawModelQuery(sql, EMPTY_BIND_VALS);
    }

    public <OBJ, PK> ResultQuery<Record> createExecutableQuery(NuSQLAccess access, Model<OBJ, PK> model) {
        return prepareQuery(access, model);
    }

    @Deprecated
    @Override
    public <OBJ, PK> ResultQuery<Record> createExecutableQuery(NuSQLAccess access, Model<OBJ, PK> model, @Nullable NamedBindValues<?> namedBindValues) {
        return super.createExecutableQuery(access, model, namedBindValues);
    }

    @Deprecated
    @Override
    public <OBJ, PK> ResultQuery<Record> createExecutableQuery(NuSQLAccess access, Model<OBJ, PK> model, @Nullable OBJ obj) {
        return super.createExecutableQuery(access, model, obj);
    }
}
