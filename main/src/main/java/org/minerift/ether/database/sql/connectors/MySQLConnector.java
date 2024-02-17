package org.minerift.ether.database.sql.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.jooq.exception.DataAccessException;
import org.minerift.ether.database.sql.DatabaseConnectionSettings;
import org.minerift.ether.database.sql.SQLAccess;
import org.minerift.ether.database.sql.SQLDatabase;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class MySQLConnector implements SQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + settings.getAddress());
        config.setUsername(settings.getUsername());
        config.setPassword(settings.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");

        return config;
    }

    @Override
    public HikariDataSource connect(SQLDatabase db, DatabaseConnectionSettings settings) {

        final int TIMEOUT = 10;

        HikariConfig noDbConfig = createConfig(settings);
        HikariConfig dbConfig = createConfig(settings);
        dbConfig.setJdbcUrl(dbConfig.getJdbcUrl() + "/" + settings.getDbName());

        // Attempt to connect to db with name
        HikariDataSource ds = null;
        try {
            ds = new HikariDataSource(dbConfig);
            if(ds.getConnection().isValid(TIMEOUT)) {
                System.out.println("Connected successfully to dbConfig");
                return ds;
            }
            throw new SQLTimeoutException("Database timed out");
        } catch (HikariPool.PoolInitializationException | SQLException ex) {
            System.out.println("Unsuccessful, retrying with noDbConfig...");
            if(ds != null) {
                System.out.println("Closing ds...");
                ds.close();
            }
        }

        // Attempt to connect to db without name and create db
        try {
            ds = new HikariDataSource(noDbConfig);
            if(ds.getConnection().isValid(TIMEOUT)) {
                System.out.println("noDbConfig connected, creating and selecting db");

                // Create and select db
                SQLAccess access = new SQLAccess(db, ds.getConnection());
                //System.out.println(access.dsl().resultQuery("SELECT database();").fetch()); // DEBUG
                access.dsl().createDatabase(settings.getDbName()).execute();
                System.out.println("Created database");

                access.close();
                ds.close();
            }
        } catch (HikariPool.PoolInitializationException | SQLException ex) {
            if(ds != null) {
                System.out.println("Closing ds...");
                ds.close();
            }
            throw new RuntimeException("Unable to connect to database", ex);
        } catch (DataAccessException dx) {
            if(ds != null) {
                System.out.println("Closing ds...");
                ds.close();
            }
            throw new RuntimeException("Failed to create database", dx);
        }

        // Attempt to connect to db with name
        try {
            ds = new HikariDataSource(dbConfig);
            if(ds.getConnection().isValid(TIMEOUT)) {
                System.out.println("Connected successfully to dbConfig!");
                return ds;
            }
            throw new SQLTimeoutException("Created database, but database timed out");
        } catch (HikariPool.PoolInitializationException | SQLException ex) {
            System.out.println("Closing ds...");
            ds.close();
            throw new RuntimeException("Created database, but failed to connect to database");
        }
    }
}
