package org.minerift.ether.database.nusql;

import org.minerift.ether.database.DataType;
import org.minerift.ether.database.nusql.connectors.*;
import org.minerift.ether.database.nusql.fallback.EnumStrFallback;
import org.minerift.ether.database.nusql.fallback.JsonFallback;
import org.minerift.ether.util.Utils;

import java.util.function.Supplier;

public enum SQLDialect {
    MYSQL       (org.jooq.SQLDialect.MYSQL,     MySQLConnector::new),
    POSTGRES    (org.jooq.SQLDialect.POSTGRES,  PostgreSQLConnector::new),
    SQLITE      (org.jooq.SQLDialect.SQLITE,    SQLiteConnector::new),
    H2          (org.jooq.SQLDialect.H2,        H2Connector::new)

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

    // For convienence
    public static SQLDialect valueOfSilent(String str) {
        return Utils.valueOfSilent(SQLDialect.class, str.toUpperCase());
    }

    private final org.jooq.SQLDialect dialect;
    private final Supplier<NuSQLConnector> dbConnector;
    SQLDialect(org.jooq.SQLDialect dialect, Supplier<NuSQLConnector> dbConnector) {
        this.dialect = dialect;
        this.dbConnector = dbConnector;
    }

    public org.jooq.SQLDialect asJooqDialect() {
        return switch (this) {
            case MYSQL -> org.jooq.SQLDialect.MYSQL;
            case POSTGRES -> org.jooq.SQLDialect.POSTGRES;
            case SQLITE -> org.jooq.SQLDialect.SQLITE;
            case H2 -> org.jooq.SQLDialect.H2;
        };
    }

    // Returns a fallback (or null if supported) for arrays
    public <T> JsonFallback<T> getArraysFallback(DataType<T> type) {
        return switch (this) {
            case POSTGRES, H2 -> null; // supported
            case MYSQL, SQLITE -> new JsonFallback<>(type.getType());
        };
    }

    // Returns a fallback (null if supported) for uuids
    // TODO: remove because jooq already supports uuids on different dialects out-of-the-box
    @Deprecated
    public <T> JsonFallback<T> getUUIDsFallback(DataType<T> type) {
        return switch(this) {
            case POSTGRES, H2 -> null; // supported
            case MYSQL, SQLITE -> new JsonFallback<>(type.getType());
        };
    }

    public NuSQLConnector getDbConnector() {
        return dbConnector.get();
    }
}
