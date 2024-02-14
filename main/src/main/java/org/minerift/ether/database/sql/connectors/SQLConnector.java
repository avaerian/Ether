package org.minerift.ether.database.sql.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.minerift.ether.database.sql.DatabaseConnectionSettings;
import org.minerift.ether.database.sql.SQLDatabase;

public interface SQLConnector {
    HikariConfig createConfig(DatabaseConnectionSettings settings);
    HikariDataSource connect(SQLDatabase db, DatabaseConnectionSettings settings);

}
