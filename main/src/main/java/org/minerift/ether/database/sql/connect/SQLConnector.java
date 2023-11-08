package org.minerift.ether.database.sql.connect;

import com.zaxxer.hikari.HikariConfig;
import org.minerift.ether.database.sql.DatabaseConnectionSettings;

public interface SQLConnector {
    HikariConfig createConfig(DatabaseConnectionSettings settings);

}
