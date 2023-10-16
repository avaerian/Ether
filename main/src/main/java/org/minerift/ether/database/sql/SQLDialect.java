package org.minerift.ether.database.sql;

public enum SQLDialect {
    MY_SQL      (org.jooq.SQLDialect.MYSQL),
    POSTGRES    (org.jooq.SQLDialect.POSTGRES),
    SQLITE      (org.jooq.SQLDialect.SQLITE),
    H2          (org.jooq.SQLDialect.H2)

    ;

    private final org.jooq.SQLDialect dialect;
    SQLDialect(org.jooq.SQLDialect dialect) {
        this.dialect = dialect;
    }

    public org.jooq.SQLDialect getWrappedDialect() {
        return dialect;
    }
}
