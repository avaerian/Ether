package org.minerift.ether.database.nusql.connect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.jooq.exception.DataAccessException;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.DatabaseException;
import org.minerift.ether.database.nusql.NuSQLAccess;
import org.minerift.ether.database.nusql.NuSQLDatabase;
import org.minerift.ether.database.sql.SQLAccess;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class MySQLConnector implements NuSQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + settings.url());
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        config.addDataSourceProperty("cachePrepStmts", "true");

        return config;
    }

    @Override
    public HikariDataSource connect(NuSQLDatabase db, DatabaseConnectionSettings settings) {

        HikariConfig noDbConfig = createConfig(settings);
        HikariConfig dbConfig = createConfig(settings);
        dbConfig.setJdbcUrl(dbConfig.getJdbcUrl() + "/" + settings.dbName());

        // Attempt to connect to db with name
        HikariDataSource ds = null;
        try {
            ds = new HikariDataSource(dbConfig);
            if(ds.getConnection().isValid(settings.timeout())) {
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
            if(ds.getConnection().isValid(settings.timeout())) {
                System.out.println("noDbConfig connected, creating and selecting db");

                // Create and select db
                NuSQLAccess access = new NuSQLAccess(db, ds.getConnection());
                //System.out.println(access.dsl().resultQuery("SELECT database();").fetch()); // DEBUG
                //TODO: access.dsl().createDatabase(settings.dbName()).execute();
                System.out.println("Created database");

                access.close();
                ds.close();
            }
        } catch (HikariPool.PoolInitializationException | SQLException ex) {
            if(ds != null) {
                System.out.println("Closing ds...");
                ds.close();
            }
            throw new DatabaseException("Unable to connect to database", ex);
        } catch (DataAccessException dx) {
            if(ds != null) {
                System.out.println("Closing ds...");
                ds.close();
            }
            throw new DatabaseException("Failed to create database", dx);
        }

        // Attempt to connect to db with name
        try {
            ds = new HikariDataSource(dbConfig);
            if(ds.getConnection().isValid(settings.timeout())) {
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
