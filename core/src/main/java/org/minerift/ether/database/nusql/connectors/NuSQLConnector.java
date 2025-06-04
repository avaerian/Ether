package org.minerift.ether.database.nusql.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.DatabaseException;
import org.minerift.ether.database.nusql.NuSQLDatabase;

public interface NuSQLConnector {
    HikariConfig createConfig(DatabaseConnectionSettings settings);
    HikariDataSource connect(NuSQLDatabase db, DatabaseConnectionSettings settings) throws DatabaseException;

}
