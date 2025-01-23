package org.minerift.ether.database;

import org.jooq.impl.SQLDataType;

// Base type for the customizable DataType
public enum PrimitiveType {

    UUID("uuid"),
    BOOLEAN,
    BYTE("tinyint"),
    SHORT("smallint"),
    INTEGER("integer"),
    BIGINT("bigint"),
    FLOAT,
    DOUBLE,

    CHAR,
    VARCHAR,
    BINARY,
    VARBINARY,

    // Enums in SQL and JDBC are annoying; instead, allow them to be represented by ordinal or string
    ENUM_ORDINAL(SHORT.getTypeName()),
    ENUM_STR(VARCHAR.getTypeName()),

    // TODO: review everything below this line

    @Deprecated CLOB,
    @Deprecated BLOB,

    @Deprecated DATE,
    @Deprecated TIME,
    @Deprecated TIMESTAMP,

    ;

    // TODO: this seems fine but decouple in future?
    private final String typeName; // maps to SQL (jOOQ) type names for translating native to jOOQ data types

    PrimitiveType() {
        this.typeName = this.name().toUpperCase();
    }

    PrimitiveType(String typeName) {
        this.typeName = typeName.toUpperCase();
    }

    public String getTypeName() {
        return typeName;
    }
}
