package org.minerift.ether.database.nusql;

import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.minerift.ether.database.*;
import org.minerift.ether.database.nusql.op.dml.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class NuSQLDatabase extends Database implements AutoCloseable {

    private final HikariDataSource dataSource;
    protected final Configuration connConfig;
    private final SQLDialect dialect;


    protected final NuDMLInsert insertQuery;
    protected final NuDMLUpdate updateQuery;
    protected final NuDMLDelete deleteQuery;
    protected final NuDMLUpsert upsertQuery;
    protected final NuDMLSelectAll selectAllQuery;
    protected final NuDMLSelectById selectByIdQuery;
    protected final NuDMLSelectAllIds selectAllIdsQuery;

    @SafeVarargs
    public NuSQLDatabase(DatabaseConnectionSettings settings, Function<DatabaseCreationContext, Model<?, ?>> ... modelCreators) throws DatabaseException {
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
            SQLDbStartupScript.run(new NuSQLAccess(this, dataSource.getConnection()));
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to run database startup script", ex);
        }
    }

    public SQLDialect getDialect() {
        return dialect;
    }

    public interface SQLAccessFunction {
        void accept(NuSQLAccess access) throws SQLException;
    }

    public CompletableFuture<SQLException> access(SQLAccessFunction proc) {
        return access(false, true, proc);
    }

    public CompletableFuture<SQLException> accessSync(SQLAccessFunction proc) {
        return access(false, false, proc);
    }

    // Flags: autocommit, async, autostart?
    public CompletableFuture<SQLException> access(boolean autocommit, boolean async, SQLAccessFunction proc) {
        // Attempt to get connection and create SQLAccess layer
        Connection conn;
        NuSQLAccess access;
        try {
            conn = dataSource.getConnection(); // Need to test connection
            if(!SQLUtils.testConnection(conn, 10)) {
                throw new SQLTimeoutException("Connection timed out!");
            }
            access = new NuSQLAccess(this, conn);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to get connection!", ex);
        }

        // Change settings for connection
        boolean oldAutocommit;
        try {
            oldAutocommit = conn.getAutoCommit();
            conn.setAutoCommit(autocommit);
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to update autocommit for connection!", ex);
        }

        Supplier<SQLException> block = () -> {
            try {
                // Attempt to execute SQL operations
                proc.accept(access);
                if(!autocommit && !access.committed) {
                    access.commit();
                }
            } catch (SQLException ex) {
                // Attempt to rollback
                if(!autocommit) {
                    ex.printStackTrace();
                    try {
                        access.rollback();
                    } catch (SQLException ex2) {
                        return new SQLException("Failed to rollback changes", ex2);
                    }
                } else {
                    return new SQLException("Failed to execute SQL operations!", ex);
                }
            }

            // Reset autocommit back to original value and close connection
            try {
                conn.setAutoCommit(oldAutocommit);
                access.close();
            } catch (SQLException ex) {
                return new SQLException("Failed to clean up resources!", ex);
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
