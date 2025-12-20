package org.minerift.ether.database.sql.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.sql.SQLDatabase;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class SQLiteConnector implements SQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:sqlite:" + settings.url() + File.separatorChar + settings.dbName() + ".db");
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        config.addDataSourceProperty("cachePrepStmts", "true");

        return config;
    }

    @Override
    public HikariDataSource connect(SQLDatabase db, DatabaseConnectionSettings settings) {
        HikariConfig config = createConfig(settings);
        try {
            HikariDataSource ds = new HikariDataSource(config);
            if(ds.getConnection().isValid(10)) {
                return ds;
            }
            throw new SQLTimeoutException("What the fuck is wrong?");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
