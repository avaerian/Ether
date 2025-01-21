package org.minerift.ether.database.nusql.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.jooq.exception.DataAccessException;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.nusql.NuSQLAccess;
import org.minerift.ether.database.nusql.NuSQLDatabase;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class PostgreSQLConnector implements NuSQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        final HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:postgresql://" + settings.getAddress().toString() + "/");
        config.setUsername(settings.getUsername());
        config.setPassword(settings.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");

        return config;
    }

    // Return connection to database
    @Override
    public HikariDataSource connect(NuSQLDatabase db, DatabaseConnectionSettings settings) {

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

                NuSQLAccess access = new NuSQLAccess(db, ds.getConnection());
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
