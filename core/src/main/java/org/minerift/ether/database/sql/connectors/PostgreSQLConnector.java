package org.minerift.ether.database.sql.connectors;

import com.google.common.net.HostAndPort;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.jooq.exception.DataAccessException;
import org.minerift.ether.Ether;
import org.minerift.ether.database.sql.*;
import org.minerift.ether.database.sql.diff.DiffType;
import org.minerift.ether.database.sql.diff.KeyDiff;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandGridV2;
import org.minerift.ether.island.IslandModel;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.user.EtherUser;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.minerift.ether.Secrets.HIDDEN;

public class PostgreSQLConnector implements SQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://" + settings.getAddress().toString() + "/");
        config.setUsername(settings.getUsername());
        config.setPassword(settings.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");

        return config;
    }

    // TODO: debug
    public static void main(String[] args) throws SQLException {

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

        DatabaseConnectionSettings mysqlSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.MYSQL)
                .setAddress(HostAndPort.fromString("localhost:3306"))
                .setDbName("ether")
                .setUsername("root")
                .setPassword("password")
                .build();

        // Attempt to connect to db
        SQLDatabase db = new SQLDatabase(postgresSettings, IslandModel::new, UserModel::new);
        HikariDataSource ds = db.getDataSource();
        System.out.println(ds == null ? "null" : ds.getConnection().isValid(10));

        Random random = new Random();
        final IslandGridV2 grid = new IslandGridV2();

        final int GRID_SIZE = 100;
        for(int i = 0; i < GRID_SIZE; i++) {
            final Island island = Island.builder()
                    .setTile(GridAlgorithm.computeTile(i), true)
                    .setOwner(EtherUser.builder().setUUID(UUID.randomUUID()).build())
                    .setDeleted(random.nextBoolean())
                    .build();
            grid.registerIsland(island);
        }

        AtomicReference<List<Island>> islands = new AtomicReference<>(Collections.emptyList());
        db.access(false, (access) -> {

            System.out.println("Current ids: " + access.selectAllIds(IslandModel.class));

            //access.insertOrUpdate(IslandModel.class, grid.getAllIslandsView());
            access.update(IslandModel.class, grid.getIslandsView());

            System.out.println("Current user ids: " + access.selectAllIds(UserModel.class));
            access.insert(UserModel.class, EtherUser.builder().setUUID(UUID.randomUUID()).build());
            System.out.println("Current user ids: " + access.selectAllIds(UserModel.class));

            SQLResult<EtherUser> sqlUsers = access.selectAll(UserModel.class);
            System.out.println(sqlUsers.stream().map(sqlUsers::readRecord).collect(Collectors.toList()));

            // TODO: review
            List<EtherUser.Builder> userBuilders = sqlUsers.stream().map(record -> (EtherUser.Builder)sqlUsers.readBuilder(record)).toList();

            // Get diffs between database ids and in-memory ids
            var diffs = KeyDiff.partitionDiffs(access.selectAllIds(IslandModel.class), Ether.getIslandManager().getKeySet());

            // From diffs, update database appropriately
            access.insert(IslandModel.class, Ether.getIslandManager().getIslands(diffs.get(DiffType.INSERTED)));
            access.update(IslandModel.class, Ether.getIslandManager().getIslands(diffs.get(DiffType.UPDATED)));
            access.deleteByIds(IslandModel.class, diffs.get(DiffType.DELETED));


            // TODO: review
            SQLResult<Island> sqlIslands = access.selectAll(IslandModel.class);
            List<Island.Builder> islandBuilders = sqlIslands.stream().map(record -> (Island.Builder)sqlIslands.readBuilder(record)).toList();

            System.out.println(sqlIslands.asResultSet());

            islands.set(sqlIslands.stream().map(sqlIslands::readRecord).collect(Collectors.toList()));
        });

        System.out.println(islands);

        db.close();
    }

    // Return connection to database
    @Override
    public HikariDataSource connect(SQLDatabase db, DatabaseConnectionSettings settings) {

        final int TIMEOUT = 10; // TODO: temporary; add to DatabaseConnectionSettings?

        HikariConfig noDbConfig = createConfig(settings);
        HikariConfig dbConfig = createConfig(settings);
        dbConfig.setJdbcUrl(dbConfig.getJdbcUrl() + settings.getDbName());

        // Attempt to connect to db with name
        try {
            HikariDataSource ds = new HikariDataSource(dbConfig);
            if(ds.getConnection().isValid(TIMEOUT)) {
                System.out.println("Connected successfully to dbConfig");
                return ds;
            }
            throw new SQLTimeoutException("Database timed out");
        } catch (HikariPool.PoolInitializationException | SQLException ex) {
            System.out.println("Unsuccessful, retrying with noDbConfig...");
        }

        // Attempt to connect to db without name and create db
        try {
            HikariDataSource ds = new HikariDataSource(noDbConfig);
            if(ds.getConnection().isValid(TIMEOUT)) {
                System.out.println("noDbConfig connected, need to create db and reconnect with dbConfig");

                SQLAccess access = new SQLAccess(db, ds.getConnection());
                access.dsl().createDatabase(settings.getDbName()).execute();
                System.out.println("Created database");

                access.close();
                ds.close();
            }
        } catch (HikariPool.PoolInitializationException | SQLException ex) {
            throw new RuntimeException("Unable to connect to database", ex);
        } catch (DataAccessException dx) {
            throw new RuntimeException("Failed to create database", dx);
        }

        // Attempt to connect to db with name
        try {
            HikariDataSource ds = new HikariDataSource(dbConfig);
            if(ds.getConnection().isValid(TIMEOUT)) {
                System.out.println("Successfully connected to dbConfig!");
                return ds;
            }
            System.out.println("Connection timed out"); // TODO: handle better
        } catch (SQLException ex) {
            throw new RuntimeException("Failed to reconnect to dbConfig; did database creation fail?", ex);
        }

        throw new RuntimeException("Unreachable");
    }
}
