package org.minerift.ether.database.sql;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.minerift.ether.database.*;
import org.minerift.ether.database.sql.op.dml.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class SQLDatabase extends Database {

    private final HikariDataSource dataSource;
    protected final Configuration connConfig;
    private final SQLDialect dialect;


    protected final DMLInsert insertQuery;
    protected final DMLUpdate updateQuery;
    protected final DMLDelete deleteQuery;
    protected final DMLUpsert upsertQuery;
    protected final DMLSelectAll selectAllQuery;
    protected final DMLSelectById selectByIdQuery;
    protected final DMLSelectAllIds selectAllIdsQuery;

    @SafeVarargs
    public SQLDatabase(DatabaseConnectionSettings settings, Function<DatabaseCreationContext, Model<?, ?>> ... modelCreators) throws DatabaseException {
        super(settings.getDbName());
        this.dialect = settings.getDialect();

        // TODO: refactor connection establishment into separate abstract method/class that can support this
        // Connect
        CompletableFuture<HikariDataSource> futureDataSource =
                supplyAsync(() -> dialect.getDbConnector().connect(this, settings));

        // Set up database
        SQLDatabaseCreationContext ctx = new SQLDatabaseCreationContext(dialect, modelCreators);
        // TODO: Register metadata model for versioning and other db metadata
        //ctx.registerModel(MetadataModel);

        this.models = ctx.getModelsMap();
        this.connConfig = ctx.connConfig;

        this.insertQuery = ctx.insertQuery;
        this.updateQuery = ctx.updateQuery;
        this.deleteQuery = ctx.deleteQuery;
        this.upsertQuery = ctx.upsertQuery;
        this.selectAllQuery = ctx.selectAllQuery;
        this.selectByIdQuery = ctx.selectByIdQuery;
        this.selectAllIdsQuery = ctx.selectAllIdsQuery;

        try {
            this.dataSource = futureDataSource.get();
        } catch (ExecutionException ex) {
            throw new DatabaseException(ex.getCause());
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }

        try {
            SQLDbStartupScript.run(new SQLAccess(this, dataSource.getConnection()));
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to run database startup script", ex);
        }
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    // Flags: autocommit, async, autostart?
    @Override
    public CompletableFuture<DatabaseException> access(boolean autocommit, boolean async, DbAccessFunction proc) {
        // Attempt to get connection and create SQLAccess layer
        Connection conn;
        SQLAccess access;
        try {
            conn = dataSource.getConnection(); // Need to test connection
            if(!SQLUtils.testConnection(conn, 10)) {
                throw new SQLTimeoutException("Connection timed out");
            }
            access = new SQLAccess(this, conn);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to get connection", ex);
        }

        // Change settings for connection
        boolean oldAutocommit;
        try {
            oldAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(autocommit);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update autocommit for connection", ex);
        }

        Supplier<DatabaseException> block = () -> {
            try {
                // Attempt to execute SQL operations
                proc.accept(access);
                if(!autocommit && !access.committed) {
                    access.commit();
                }
            } catch (DatabaseException ex) {
                // Attempt to rollback
                if(!autocommit) {
                    ex.printStackTrace();
                    try {
                        access.rollback();
                    } catch (DatabaseException ex2) {
                        return ex2;
                    }
                } else {
                    return ex;
                }
            }

            // Reset autocommit back to original value and close connection
            try {
                conn.setAutoCommit(oldAutocommit);
                access.close();
            } catch (SQLException ex) {
                return new DatabaseException("Failed to clean up resources", ex);
            }

            return null; // no exception; everything completed successfully
        };

        if(async) {
            return supplyAsync(block);
        } else {
            return completedFuture(block.get());
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    // Returns the DSLContext object from the Configuration.
    // This context is only used for query building/rendering SQL
    @Deprecated
    public DSLContext dsl() {
        return connConfig.dsl();
    }

    @Override
    public void close() {
        dataSource.close();
    }

    /***
     * TODO: Desired Behavior for Pushing Updates to Database (autosave)
     * - Synchronously create query and bind every object to it (Islands, Users, Island Invites, etc.)
     * - Asynchronously execute query and handle appropriately
     */
}
