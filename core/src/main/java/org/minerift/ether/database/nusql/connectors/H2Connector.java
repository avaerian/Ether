package org.minerift.ether.database.nusql.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.nusql.NuSQLDatabase;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class H2Connector implements NuSQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:h2:~/" + settings.getDbName());
        config.setUsername(settings.getUsername());
        config.setPassword(settings.getPassword());
        //config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("DATABASE_TO_UPPER", "false");
        config.addDataSourceProperty("CASE_INSENSITIVE_IDENTIFIERS", "true");

        return config;
    }

    @Override
    public HikariDataSource connect(NuSQLDatabase db, DatabaseConnectionSettings settings) {
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
