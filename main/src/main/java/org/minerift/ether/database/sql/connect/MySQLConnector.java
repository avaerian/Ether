package org.minerift.ether.database.sql.connect;

import com.zaxxer.hikari.HikariConfig;
import org.minerift.ether.database.sql.DatabaseConnectionSettings;

public class MySQLConnector implements SQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        return null;
    }
}
