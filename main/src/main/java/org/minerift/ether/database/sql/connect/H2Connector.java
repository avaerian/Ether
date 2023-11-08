package org.minerift.ether.database.sql.connect;

import com.zaxxer.hikari.HikariConfig;
import org.minerift.ether.database.sql.DatabaseConnectionSettings;

public class H2Connector implements SQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:h2:~/" + settings.getDbName());
        config.setUsername(settings.getUsername());
        config.setPassword(settings.getPassword());
        config.addDataSourceProperty("cachePrepStmts", "true");

        return config;
    }
}
