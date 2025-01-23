package org.minerift.ether.database.nusql.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.nusql.NuSQLDatabase;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class H2Connector implements NuSQLConnector {
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        HikariConfig config = new HikariConfig();

        // TODO: review connecting via TCP -> https://www.h2database.com/html/tutorial.html
        //config.setJdbcUrl("jdbc:h2:~/" + settings.getDbName());

        // TODO: implement unit tests for different JDBC urls with embedded dbs (?)
        config.setJdbcUrl("jdbc:h2:" + settings.getUrl() + File.separatorChar + settings.getDbName());
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
