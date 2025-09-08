package org.minerift.ether.database.sql;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.conf.BackslashEscaping;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.minerift.ether.database.DatabaseCreationContext;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.sql.op.dml.*;
import org.minerift.ether.debug.Debug;

import java.util.*;
import java.util.function.Function;

public class SQLDatabaseCreationContext extends DatabaseCreationContext {

    protected final Configuration connConfig;
    protected final DSLContext dsl;

    protected final SQLDialect dialect;

    protected final DMLInsert insertQuery;
    protected final DMLUpdate updateQuery;
    protected final DMLDelete deleteQuery;
    protected final DMLUpsert upsertQuery;
    protected final DMLSelectAll selectAllQuery;
    protected final DMLSelectById selectByIdQuery;
    protected final DMLSelectAllIds selectAllIdsQuery;

    @SafeVarargs
    public SQLDatabaseCreationContext(SQLDialect dialect, Function<DatabaseCreationContext, Model<?, ?>> ... models) {
        super(new LinkedHashMap<>(models.length));
        this.dialect = dialect;

        // Configure + setup jOOQ
        Settings connSettings = new Settings()
                .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_QUOTED)
                .withRenderNameCase(RenderNameCase.AS_IS)
                .withBackslashEscaping(BackslashEscaping.OFF);

        this.connConfig = new DefaultConfiguration();
        connConfig.set(dialect.asJooqDialect());
        connConfig.set(connSettings);
        this.dsl = connConfig.dsl();

        registerModels(models);

        // Create/cache queries for DML operations
        this.insertQuery = new DMLInsert(this);
        this.updateQuery = new DMLUpdate(this);
        this.deleteQuery = new DMLDelete(this);
        this.upsertQuery = new DMLUpsert(this);
        this.selectAllQuery = new DMLSelectAll(this);
        this.selectByIdQuery = new DMLSelectById(this);
        this.selectAllIdsQuery = new DMLSelectAllIds(this);
    }

    public SQLDialect dialect() {
        return dialect;
    }

    public DSLContext dsl() {
        return dsl;
    }

    @Debug
    public List<DMLOp> getDMLOps() {
        return List.of(insertQuery, updateQuery, deleteQuery, upsertQuery, selectAllQuery, selectByIdQuery, selectAllIdsQuery);
    }
}
