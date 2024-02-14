package org.minerift.ether.database.sql;

import org.minerift.ether.database.sql.connectors.*;

import java.util.function.Supplier;

public enum SQLDialect {
    MYSQL       (org.jooq.SQLDialect.MYSQL,     true,   MySQLConnector::new),
    POSTGRES    (org.jooq.SQLDialect.POSTGRES,  true,   PostgreSQLConnector::new),
    SQLITE      (org.jooq.SQLDialect.SQLITE,    false,  SQLiteConnector::new),
    H2          (org.jooq.SQLDialect.H2,        true,   H2Connector::new)

    ;

    public static SQLDialect adapt(org.jooq.SQLDialect dialect) {
        return switch (dialect) {
            case MYSQL      -> MYSQL;
            case POSTGRES   -> POSTGRES;
            case SQLITE     -> SQLITE;
            case H2         -> H2;
            default -> throw new IllegalArgumentException("Unable to adapt unsupported dialect " + dialect.getName());
        };
    }

    private final org.jooq.SQLDialect dialect;
    private final boolean supportsArrays;
    private final Supplier<SQLConnector> dbConnector;
    SQLDialect(org.jooq.SQLDialect dialect, boolean supportsArrays, Supplier<SQLConnector> dbConnector) {
        this.dialect = dialect;
        this.supportsArrays = supportsArrays;
        this.dbConnector = dbConnector;
    }

    public org.jooq.SQLDialect asJooqDialect() {
        return dialect;
    }

    public boolean supportsArrays() {
        return supportsArrays;
    }

    public SQLConnector getDbConnector() {
        return dbConnector.get();
    }
}
