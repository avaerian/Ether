package org.minerift.ether.database.sql;

//import org.jooq.util.sqlite.SQLiteDataType; // saved as a reference
import org.minerift.ether.util.Utils;

import java.util.LinkedList;

import static org.minerift.ether.database.sql.SQLDataType.Param.*;

public enum SQLDataType {

    /**
     * TODO for DB lib:
     *  - handle data types with multiple idents when parsing CREATE TABLE query
     *  - allow for getting appropriate data type from type name (from SQL result; for all dialects)
     *  - coerce db metadata into a common data structure for handling (for all dialects)
     *  - polish CREATE TABLE parser
     */

    TINYINT("tinyint"),
    SMALLINT("smallint"),
    INT2("int2"),
    INT("int"),
    INTEGER("integer"),
    MEDIUMINT("mediumint"),
    INT8("int8"),
    BIGINT("bigint"),
    UNSIGNED_BIG_INT("unsigned big int"),
    DOUBLE("double"),
    DOUBLE_PRECISION("double precision"),
    REAL("real"),
    FLOAT("float"),
    NUMERIC("numeric(p, s)"),
    DECIMAL("decimal(p, s)"),
    CHAR("char(l)"),
    CHARACTER("character(l)"),
    LONGVARCHAR("longvarchar(l)"),
    VARCHAR("varchar(l)"),
    VARYING_CHARACTER("varying character(l)"),
    NCHAR(""),
    NATIVE_CHARACTER(""),
    NVARCHAR(""),
    CLOB(""),
    TEXT(""),
    BOOLEAN("boolean"),
    DATE(""),
    DATETIME(""),
    BLOB("")

    ;

    public enum Param {
        LEN, PRECISION, SCALE
    }

    private final String name;
    private final Param[] params;
    SQLDataType(String name) {
        //SQLiteDataType
        this.name = name;
        int i = name.indexOf('(');
        LinkedList<Param> params = new LinkedList<>();
        if(i++ != -1) {
            char c = name.charAt(i++);
            do {
                Param param = switch (c) {
                    case 'l' -> LEN;
                    case 'p' -> PRECISION;
                    case 's' -> SCALE;
                    default -> throw new IllegalStateException("Unexpected parameter \"" + c + "\"");
                };
                if(name.charAt(i) == ',') {
                    i += 2; // COMMA + SPACE
                }
            } while (name.charAt(i++) != name.indexOf(')', i));
        }
        this.params = params.toArray(Param[]::new);
    }

    public Param[] getParams() {
        return params;
    }

    public boolean hasParam(Param param) {
        return Utils.contains(params, param);
    }

    public boolean hasLen() {
        return hasParam(LEN);
    }

    public boolean hasPrecision() {
        return hasParam(PRECISION);
    }

    public boolean hasScale() {
        return hasParam(SCALE);
    }

}
