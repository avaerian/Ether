package org.minerift.ether.database.sql;

import com.google.common.net.HostAndPort;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.impl.DefaultConfiguration;
import org.minerift.ether.database.sql.metadata.Metadata;
import org.minerift.ether.database.sql.metadata.MetadataModel;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.ddl.DDLGetTables;
import org.minerift.ether.database.sql.op.dml.*;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandGrid;
import org.minerift.ether.island.IslandModel;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.user.EtherUser;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.*;
import java.util.function.Function;

import static org.minerift.ether.database.sql.DatabasePlayground.HIDDEN;

public class SQLDatabase implements AutoCloseable {

    public static void main(String[] args) throws Exception {

        // TODO: use jOOQ to see supported data types for dialects
        //SQLDataType.UUID

        DatabaseConnectionSettings postgresSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.POSTGRES)
                .setAddress(HostAndPort.fromHost("localhost"))
                .setDbName("ether")
                .setUsername("postgres")
                .setPassword(HIDDEN)
                .build();

        DatabaseConnectionSettings h2Settings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.H2)
                .setDbName("ether")
                .setUsername("root")
                .setPassword("")
                .build();

        DatabaseConnectionSettings sqliteSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.SQLITE)
                .setDbName("ether")
                .setUsername("root")
                .setPassword("")
                .build();

        SQLDatabase db = new SQLDatabase(sqliteSettings, IslandModel::new);

        IslandModel model = db.getModel(IslandModel.class);

        Random random = new Random();

        final int GRID_SIZE = 100;
        IslandGrid grid = new IslandGrid();
        for(int i = 0; i < GRID_SIZE; i++) {
            final Island island = Island.builder()
                    .setTile(GridAlgorithm.computeTile(i), true)
                    .setOwner(EtherUser.builder().setUUID(UUID.randomUUID()).build())
                    .setDeleted(random.nextBoolean())
                    .build();
            grid.registerIsland(island);
        }

        var islandsView = grid.getIslandsView();

        db.access(true, (access) -> {
            access.insert(IslandModel.class, (Island)null);
            access.update(MetadataModel.class, (Metadata)null);

            SQLResult<Island> islandsResult = access.selectAll(IslandModel.class);
            for(Record record : islandsResult) {
                Island island = islandsResult.readRecord(record);
                //int id              = islandsResult.getField(model.ID, record);
                //boolean isDeleted   = islandsResult.getField(model.IS_DELETED, record);
                //UUID[] members      = islandsResult.getField(model.MEMBERS, record);
            }
        });

        db.close();
    }

    private final HikariDataSource dataSource;
    public final Configuration connConfig;
    private final String dbName;
    private final SQLDialect dialect;
    private final Map<Class<? extends Model>, Model<?, ?>> models;


    // Query Caches
    public final DMLInsert INSERT_QUERY;
    public final DMLUpdate UPDATE_QUERY;
    public final DMLUpsert UPSERT_QUERY;
    public final DMLDelete DELETE_QUERY;
    public final DMLSelectAll SELECT_ALL_QUERY;
    public final DMLSelectById SELECT_ID_QUERY;


    public SQLDatabase(DatabaseConnectionSettings settings, Function<SQLDatabase, Model<?, ?>> ... models) {

        // Init db object
        this.dbName = settings.getDbName();
        this.dialect = settings.getDialect();

        this.connConfig = new DefaultConfiguration();
        connConfig.set(settings.getDialect().asJooqDialect());

        this.models = new HashMap<>(models.length);
        for(var tableSupplier : models) {
            var table = tableSupplier.apply(this);
            this.models.put(table.getClass(), table);
        }

        this.INSERT_QUERY = new DMLInsert(this);
        this.UPDATE_QUERY = new DMLUpdate(this);
        this.UPSERT_QUERY = new DMLUpsert(this);
        this.DELETE_QUERY = new DMLDelete(this);
        this.SELECT_ALL_QUERY = new DMLSelectAll(this);
        this.SELECT_ID_QUERY = new DMLSelectById(this);

        // Connect to db
        // TODO: SQLConnector connector = settings.getDialect().getDbConnector();
        this.dataSource = settings.getDialect().getDbConnector().connect(this, settings);

        try {
            SQLDbStartupScript.run(new SQLAccess(this, dataSource.getConnection()));
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to run database startup script", ex);
        }
    }

    public String getDbName() {
        return dbName;
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public interface SQLAccessFunction {
        void accept(SQLAccess access) throws SQLException;
    }

    // NOTE: this impl restricts transactions to being simple, single layer (no transactions inside of transactions)
    // This is fine for now, but may be a desireable feature in the future
    // If a transaction, every command will be executed together
    // If not a transaction, every command will be autocommitted
    public void access(boolean transaction, SQLAccessFunction proc) {
        // Attempt to get connection and create SQLAccess layer
        Connection conn;
        SQLAccess access;
        try {
            conn = dataSource.getConnection(); // Need to test connection
            if(!SQLUtils.testConnection(conn, 10)) {
                throw new SQLTimeoutException("Connection timed out!");
            }
            access = new SQLAccess(this, conn);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to get connection!", ex);
        }

        // Change settings for connection
        boolean autocommit;
        try {
            autocommit = conn.getAutoCommit();
            conn.setAutoCommit(!transaction); // if transaction = true, autocommit = false
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update autocommit for connection!", ex);
        }

        try {
            // Attempt to execute SQL operations
            proc.accept(access);
            if(transaction) {
                access.commit();
            }
        } catch (SQLException ex) {
            // Attempt to rollback
            if(transaction) {
                ex.printStackTrace();
                try {
                    access.rollback();
                } catch (SQLException ex2) {
                    throw new RuntimeException("Failed to rollback changes!", ex2);
                }
            } else {
                throw new RuntimeException("Failed to execute SQL operations!", ex);
            }
        }

        // Reset autocommit back to original value and close connection
        try {
            conn.setAutoCommit(autocommit);
            access.close();
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to clean up resources!", ex);
        }
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    // Returns the DSLContext object from the Configuration.
    // This context is only used for query building/rendering SQL
    public DSLContext dsl() {
        return connConfig.dsl();
    }

    public <M extends Model> M getModel(Class<M> modelClazz) {
        return (M) models.get(modelClazz);
    }

    public Collection<Model<?, ?>> getModels() {
        return models.values();
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
