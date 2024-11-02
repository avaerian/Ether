package org.minerift.ether.database;

// Base type for the customizable DataType
public enum PrimitiveType {

    UUIDv4("uuid"), // TODO: support more UUID versions
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

    // TODO: review everything below this line

    CLOB,
    BLOB,

    DATE,
    TIME,
    TIMESTAMP,

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
