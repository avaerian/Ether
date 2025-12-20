package org.minerift.ether.database.sql;

//import org.jooq.util.sqlite.SQLiteDataType; // saved as a reference
import org.minerift.ether.debug.Debug;
import org.minerift.ether.util.Utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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

    private static final Param[] NO_PARAMS = new Param[0];

    private final String name;
    private final String rawName;
    private final Param[] params;
    SQLDataType(String name) {
        //SQLiteDataType
        // numeric(p, s)
        this.rawName = name;
        int i = name.indexOf('(');
        if(i == -1) { // not found
            this.name = name;
            this.params = null;
        } else { // found
            List<Param> params = new LinkedList<>();

            this.name = name.substring(0, i++); // move forward from the '('
            while(name.charAt(i) != ')') {
                Param param = switch (name.charAt(i)) {
                    case 'l' -> LEN;
                    case 'p' -> PRECISION;
                    case 's' -> SCALE;
                    default -> throw new IllegalStateException("Unexpected parameter \"" + name.charAt(i) + "\"");
                };
                params.add(param);
                i++; // move forward from the param

                if(name.charAt(i) == ',') {
                    i++; // move away from possible comma
                    if(name.charAt(i) == ' ') {
                        i++; // move away from possible space
                    }
                }
            }

            this.params = params.toArray(Param[]::new);
        }
        System.out.printf("%s (%s) -> %s\n", this.name, rawName, Arrays.toString(params));
    }

    public String getName() {
        return name;
    }

    public String getRawName() {
        return rawName;
    }

    public Param[] getParams() {
        return params == null ? NO_PARAMS : params;
    }

    public boolean hasParam(Param param) {
        return params != null && Utils.contains(params, param);
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
