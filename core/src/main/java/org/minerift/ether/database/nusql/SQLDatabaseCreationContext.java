package org.minerift.ether.database.nusql;

import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.conf.BackslashEscaping;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.minerift.ether.database.DatabaseCreationContext;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.op.dml.*;
import org.minerift.ether.debug.Debug;

import java.util.*;
import java.util.function.Function;

public class SQLDatabaseCreationContext extends DatabaseCreationContext {

    protected final Configuration connConfig;
    protected final DSLContext dsl;

    protected final SQLDialect dialect;

    protected final NuDMLInsert insertQuery;
    protected final NuDMLUpdate updateQuery;
    protected final NuDMLDelete deleteQuery;
    protected final NuDMLUpsert upsertQuery;
    protected final NuDMLSelectAll selectAllQuery;
    protected final NuDMLSelectById selectByIdQuery;
    protected final NuDMLSelectAllIds selectAllIdsQuery;

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
        this.insertQuery = new NuDMLInsert(this);
        this.updateQuery = new NuDMLUpdate(this);
        this.deleteQuery = new NuDMLDelete(this);
        this.upsertQuery = new NuDMLUpsert(this);
        this.selectAllQuery = new NuDMLSelectAll(this);
        this.selectByIdQuery = new NuDMLSelectById(this);
        this.selectAllIdsQuery = new NuDMLSelectAllIds(this);
    }

    public SQLDialect dialect() {
        return dialect;
    }

    public DSLContext dsl() {
        return dsl;
    }

    @Debug
    public List<NuDMLOp> getDMLOps() {
        return List.of(insertQuery, updateQuery, deleteQuery, upsertQuery, selectAllQuery, selectByIdQuery, selectAllIdsQuery);
    }
}
