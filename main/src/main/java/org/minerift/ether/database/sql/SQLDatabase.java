package org.minerift.ether.database.sql;

import com.google.common.net.HostAndPort;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.Configuration;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.conf.BackslashEscaping;
import org.jooq.conf.RenderNameCase;
import org.jooq.conf.RenderQuotedNames;
import org.jooq.conf.Settings;
import org.jooq.impl.DefaultConfiguration;
import org.minerift.ether.Ether;
import org.minerift.ether.database.sql.adapters.Adapters;
import org.minerift.ether.database.sql.diff.DiffType;
import org.minerift.ether.database.sql.diff.KeyDiff;
import org.minerift.ether.database.sql.metadata.MetadataModel;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.dml.*;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandGridV2;
import org.minerift.ether.island.IslandModel;
import org.minerift.ether.island.invites.InviteRegistry;
import org.minerift.ether.island.invites.IslandInvite;
import org.minerift.ether.island.invites.IslandInvitesModel;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.pair.SameTypePair;
import org.minerift.ether.util.pair.UUIDPair;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.minerift.ether.Secrets.HIDDEN;

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

        SQLDatabase db = new SQLDatabase(sqliteSettings, IslandModel::new, IslandInvitesModel::new);

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
                /*if(invite.isExpired()) {
                    access.deleteById(IslandInvitesModel.class, invitesModel.SENDER_RECEIVER.readField(invite));
                }*/
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

    public static SQLDatabase debug(SQLDialect dialect) {
        return new SQLDatabase(dialect);
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
    public final DMLSelectAllIds SELECT_ALL_IDS_QUERY;
    public final DMLSelectById SELECT_ID_QUERY;

    // debug ctor used for bootstrapping Model objects
    private SQLDatabase(SQLDialect dialect) {
        this.dbName = "debug";
        this.dialect = dialect;
        this.models = Collections.emptyMap();

        this.connConfig = null;
        this.dataSource = null;

        this.INSERT_QUERY = null;
        this.UPDATE_QUERY = null;
        this.UPSERT_QUERY = null;
        this.DELETE_QUERY = null;
        this.SELECT_ID_QUERY = null;
        this.SELECT_ALL_QUERY = null;
        this.SELECT_ALL_IDS_QUERY = null;
    }

    @SafeVarargs
    public SQLDatabase(DatabaseConnectionSettings settings, Function<SQLDatabase, Model<?, ?>> ... models) {
        // Init db object
        this.dbName = settings.getDbName();
        this.dialect = settings.getDialect();

        // Configure Jooq render settings
        Settings connSettings = new Settings()
                .withRenderQuotedNames(RenderQuotedNames.EXPLICIT_DEFAULT_QUOTED)
                .withRenderNameCase(RenderNameCase.AS_IS)
                .withBackslashEscaping(BackslashEscaping.OFF);

        this.connConfig = new DefaultConfiguration();
        connConfig.set(settings.getDialect().asJooqDialect());
        connConfig.set(connSettings);

        // Register models
        this.models = new HashMap<>(models.length);
        for(var tableSupplier : models) {
            var table = tableSupplier.apply(this);
            this.models.put(table.getClass(), table);
        }

        // Register metadata model for versioning and other db metadata
        this.models.putIfAbsent(MetadataModel.class, new MetadataModel(this));

        // Cache DML queries
        this.INSERT_QUERY = new DMLInsert(this);
        this.UPDATE_QUERY = new DMLUpdate(this);
        this.UPSERT_QUERY = new DMLUpsert(this);
        this.DELETE_QUERY = new DMLDelete(this);
        this.SELECT_ALL_QUERY = new DMLSelectAll(this);
        this.SELECT_ALL_IDS_QUERY = new DMLSelectAllIds(this);
        this.SELECT_ID_QUERY = new DMLSelectById(this);

        // Connect to db
        this.dataSource = settings.getDialect().getDbConnector().connect(this, settings);

        try {
            SQLDbStartupScript.run(new SQLAccess(this, dataSource.getConnection()));
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to run database startup script", ex);
        }
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
