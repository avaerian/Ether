package org.minerift.ether.database.sql;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.CreateDatabaseIfNotExists;
import org.minerift.ether.database.sql.operations.CreateTableIfNotExists;
import org.minerift.ether.database.sql.operations.dml.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SQLContext {

    private final Map<Class<? extends Model>, Model<?>> tables; // tables handled under this context
    private final SQLDialect dialect;
    private final DSLContext ctx;

    // TODO:
    //  - DDLCreateTableIfNotExists     // standard, self-explanatory
    //  - DDLUpgradeTableToNewSchema    // creates a new table with the updated schema and copies data over using a TableUpgrader
    //      - for table upgrading, the changes between the old and new table schemas would need to be tracked
    //      - columns with the no changes could be easily copied over
    //      - ModelReader subclasses will need to handle versioning (ModelReader reads an SQL row as a modelled object)
    //  - DDLAddColumn                  // adds a column to an existing SQL table
    //  - DDLDelColumn                  // deletes a column from an existing SQL table
    //  - DDLRenameColumn
    @Deprecated public CreateDatabaseIfNotExists createDbQuery;
    @Deprecated public CreateTableIfNotExists createTableQuery;

    // DML queries
    public final DMLInsert insertQuery;
    public final DMLUpdate updateQuery;
    public final DMLInsertOrUpdate upsertQuery;
    public final DMLSelectAll selectAllQuery;
    public final DMLSelectObject selectQuery;


    public SQLContext(SQLDatabase db, SQLDialect dialect, Function<SQLContext, Model<?>> ... tables) {
        this.ctx = DSL.using(db.getDataSource(), dialect.getWrappedDialect());
        this.dialect = dialect;

        // Register tables
        this.tables = new HashMap<>();
        for(var tableSupplier : tables) {
            Model<?> table = tableSupplier.apply(this);
            this.tables.put(table.getClass(), table);
        }

        // Register/cache queries
        this.createDbQuery      = new CreateDatabaseIfNotExists(this);
        this.createTableQuery   = new CreateTableIfNotExists(this);

        this.insertQuery        = new DMLInsert(this);
        this.updateQuery        = new DMLUpdate(this);
        this.upsertQuery        = new DMLInsertOrUpdate(this);
        this.selectAllQuery     = new DMLSelectAll(this);
        this.selectQuery        = new DMLSelectObject(this);
    }

    public DSLContext dsl() {
        return ctx;
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public <M extends Model> M getTable(Class<M> modelClazz) {
        return (M) tables.get(modelClazz);
    }

    public Collection<Model<?>> getTables() {
        return tables.values();
    }
}
