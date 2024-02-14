package org.minerift.ether.database.sql.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.minerift.ether.database.sql.DatabaseConnectionSettings;
import org.minerift.ether.database.sql.SQLDatabase;

public class MySQLConnector implements SQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        return null;
    }

    @Override
    public HikariDataSource connect(SQLDatabase db, DatabaseConnectionSettings settings) {
        return null;
    }
}
