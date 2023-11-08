package org.minerift.ether.database.sql.connect;

import com.zaxxer.hikari.HikariConfig;
import org.minerift.ether.database.sql.DatabaseConnectionSettings;

public class SQLiteConnector implements SQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:sqlite:" + settings.getDbName() + ".db");
        config.setUsername(settings.getUsername());
        config.setPassword(settings.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");

        return config;
    }
}
