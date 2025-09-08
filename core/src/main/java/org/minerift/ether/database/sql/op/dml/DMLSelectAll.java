package org.minerift.ether.database.sql.op.dml;

import org.jetbrains.annotations.Nullable;
import org.jooq.Record;
import org.jooq.ResultQuery;
import org.minerift.ether.database.sql.SQLDatabaseCreationContext;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.sql.SQLAccess;
import org.minerift.ether.database.sql.op.bind.NamedBindValues;

import static org.minerift.ether.database.sql.SQLUtils.EMPTY_BIND_VALS;
import static org.minerift.ether.database.sql.SQLUtils.asJooqTable;

public class DMLSelectAll extends DMLSelectOp {
    public DMLSelectAll(SQLDatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected <OBJ, PK> RawModelQuery createQueryForModel(SQLDatabaseCreationContext ctx, Model<OBJ, PK> model) {
        String sql = ctx.dsl().select().from(asJooqTable(model)).getSQL();
        return new RawModelQuery(sql, EMPTY_BIND_VALS);
    }

    public <OBJ, PK> ResultQuery<Record> createExecutableQuery(SQLAccess access, Model<OBJ, PK> model) {
        return prepareQuery(access, model);
    }

    @Deprecated
    @Override
    public <OBJ, PK> ResultQuery<Record> createExecutableQuery(SQLAccess access, Model<OBJ, PK> model, @Nullable NamedBindValues<?> namedBindValues) {
        return super.createExecutableQuery(access, model, namedBindValues);
    }

    @Deprecated
    @Override
    public <OBJ, PK> ResultQuery<Record> createExecutableQuery(SQLAccess access, Model<OBJ, PK> model, @Nullable OBJ obj) {
        return super.createExecutableQuery(access, model, obj);
    }
}
