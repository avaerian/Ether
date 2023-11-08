package org.minerift.ether.database.sql.connect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.minerift.ether.database.sql.DatabaseConnectionSettings;

import java.sql.SQLException;

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

    // TODO: psuedo-code for connector
    public void tryConnect() throws SQLException {
        try {
            var ds = new HikariDataSource(createConfig(null));
            ds.getConnection().isValid(5);
        } catch (SQLException ex) {
            // Connect to db without db, create, and reconnect
        }
    }
}
