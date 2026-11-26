package org.minerift.ether.database.nusql.connect;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.DatabaseException;
import org.minerift.ether.database.nusql.NuSQLDatabase;

import java.io.File;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

public class H2Connector implements NuSQLConnector {

    //review connecting via TCP -> https://www.h2database.com/html/tutorial.html
    @Override
    public HikariConfig createConfig(DatabaseConnectionSettings settings) {
        HikariConfig config = new HikariConfig();

        //config.setJdbcUrl("jdbc:h2:~/" + settings.getDbName());

        config.setDriverClassName("org.h2.Driver");
        config.setJdbcUrl("jdbc:h2:" + settings.url() + File.separatorChar + settings.dbName());
        config.setUsername(settings.username());
        config.setPassword(settings.password());
        //config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("DATABASE_TO_UPPER", "false"); //TODO: review
        config.addDataSourceProperty("CASE_INSENSITIVE_IDENTIFIERS", "true");

        return config;
    }

    @Override
    public HikariDataSource connect(NuSQLDatabase db, DatabaseConnectionSettings settings) throws DatabaseException {
        HikariConfig config = createConfig(settings);
        try {
            HikariDataSource ds = new HikariDataSource(config);
            if(ds.getConnection().isValid(settings.timeout())) {
                return ds;
            }
            throw new SQLTimeoutException("What the fuck is wrong?");
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }
}
