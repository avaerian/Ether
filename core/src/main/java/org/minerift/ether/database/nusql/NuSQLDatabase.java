package org.minerift.ether.database.nusql;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.DatabaseCreationContext;
import org.minerift.ether.database.DatabaseException;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.op.dml.*;
import org.minerift.ether.database.sql.*;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class NuSQLDatabase implements AutoCloseable {

    private final HikariDataSource dataSource;
    protected final Configuration connConfig;
    private final String dbName;
    private final SQLDialect dialect;
    private final Map<Class<? extends Model>, Model<?, ?>> models;


    protected final NuDMLInsert insertQuery;
    protected final NuDMLUpdate updateQuery;
    protected final NuDMLDelete deleteQuery;
    protected final NuDMLUpsert upsertQuery;
    protected final NuDMLSelectAll selectAllQuery;
    protected final NuDMLSelectById selectByIdQuery;
    protected final NuDMLSelectAllIds selectAllIdsQuery;

    @SafeVarargs
    public NuSQLDatabase(DatabaseConnectionSettings settings, Function<DatabaseCreationContext, Model<?, ?>> ... modelCreators) throws DatabaseException {
        this.dbName = settings.getDbName();
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

        /*
        try {
            SQLDbStartupScript.run(new SQLAccess(this, dataSource.getConnection()));
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to run database startup script", ex);
        }
        */
    }

    public String getName() {
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
    /*
    // TODO: refactor this so that the NuSQLAccess proc can be run with a specific scheduler/run later (in effort to move towards a more reactive system?)
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
    */

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    // Returns the DSLContext object from the Configuration.
    // This context is only used for query building/rendering SQL
    @Deprecated
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

    /***
     * TODO: Desired Behavior for Pushing Updates to Database (autosave)
     * - Synchronously create query and bind every object to it (Islands, Users, Island Invites, etc.)
     * - Asynchronously execute query and handle appropriately
     */


    // Playground for testing NuSQLDatabase
    /*
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

        NuSQLDatabase db = new NuSQLDatabase(sqliteSettings, IslandModel::new, IslandInvitesModel::new);

        IslandModel model = db.getModel(IslandModel.class);
        IslandInvitesModel invitesModel = db.getModel(IslandInvitesModel.class);

        Random random = new Random();

        final int GRID_SIZE = 100;
        IslandGridV2 grid = new IslandGridV2();
        for(int i = 0; i < GRID_SIZE; i++) {
            final Island island = Island.builder()
                    .setTile(GridAlgorithm.computeTile(i), true)
                    .setOwner(EtherUser.builder().setUUID(UUID.randomUUID()).build())
                    .setDeleted(random.nextBoolean())
                    .build();
            grid.registerIsland(island);
        }

        Ether.Debug.setIslandGrid(grid);
        Ether.Debug.setLogger(Logger.getGlobal());
        var islandsView = grid.getData();

        InviteRegistry inviteRegistry = new InviteRegistry();

        IslandInvite randomInvite = new IslandInvite(UUID.randomUUID(), UUID.randomUUID(), islandsView.get(random.nextInt(GRID_SIZE)), System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1));

        db.access(false, (access) -> {

            access.insert(IslandInvitesModel.class, randomInvite);
            //access.update(MetadataModel.class, (Metadata)null);

            // Read and register all invites
            SQLResult<IslandInvite> invitesResult = access.selectAll(IslandInvitesModel.class);
            for(Record record : invitesResult) {
                IslandInvite invite = invitesResult.readRecord(record);
                inviteRegistry.register(invite);
                System.out.println(invite);
                System.out.println(invite.isExpired());
                //if(invite.isExpired()) {
                //    access.deleteById(IslandInvitesModel.class, invitesModel.SENDER_RECEIVER.readField(invite));
                //}
            }

            inviteRegistry.purgeExpiredInvites();

            // Get differences
            Set<UUIDPair> dbInviteKeys = access.selectAllIds(IslandInvitesModel.class, Adapters.PAIR_2_UUIDS);

            System.out.println(Arrays.deepToString(dbInviteKeys.toArray(SameTypePair[]::new)));
            System.out.println(Arrays.deepToString(inviteRegistry.getKeySet().toArray(SameTypePair[]::new)));
            Map<DiffType, List<UUIDPair>> inviteDiffs = KeyDiff.partitionDiffs(dbInviteKeys, inviteRegistry.getKeySet());
            System.out.println(inviteDiffs);

            // Remove deleted ids
            if(!inviteDiffs.getOrDefault(DiffType.DELETED, Collections.emptyList()).isEmpty()) {
                access.deleteByIds(IslandInvitesModel.class, inviteDiffs.get(DiffType.DELETED).stream().map(UUIDPair::toArray).collect(Collectors.toList()));
            }
        });

        db.close();
    }
    */
}
