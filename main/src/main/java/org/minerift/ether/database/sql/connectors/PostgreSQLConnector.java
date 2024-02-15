package org.minerift.ether.database.sql.connectors;

import com.google.common.net.HostAndPort;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.jooq.exception.DataAccessException;
import org.minerift.ether.database.sql.DatabaseConnectionSettings;
import org.minerift.ether.database.sql.SQLAccess;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.island.IslandModel;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

import static org.minerift.ether.database.sql.DatabasePlayground.HIDDEN;

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
        SQLDatabase db = new SQLDatabase(mysqlSettings, IslandModel::new);
        HikariDataSource ds = db.getDataSource();
        System.out.println(ds == null ? "null" : ds.getConnection().isValid(10));
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
