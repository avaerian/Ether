package org.minerift.ether.database.sql.op.ddl;

import org.jooq.DSLContext;
import org.jooq.DataType;
import org.jooq.Field;
import org.jooq.impl.SQLDataType;
import org.minerift.ether.database.sql.SQLAccess;

import java.util.Set;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class DDLGetTables {

    // Get list of table names in a database
    // All table names are UPPERCASE
    public static Set<String> getDatabaseTables(SQLAccess access) {
        final DataType<String> TABLE_NAME_TYPE = SQLDataType.VARCHAR;
        final DSLContext dsl = access.dsl();
        Set<String> tableNames = switch(access.dialect()) {
            case POSTGRES -> {
                final Field<String> TABLE_NAME = field("table_name", TABLE_NAME_TYPE);
                yield dsl.select(TABLE_NAME).from("information_schema.tables")
                        .where(field("table_schema").eq("public"))
                        .and(field("table_type").eq("BASE TABLE"))
                        .fetchSet(TABLE_NAME);
            }
            case MYSQL -> {
                final Field<String> TABLE_NAME = field("table_name", TABLE_NAME_TYPE);
                yield dsl.select(TABLE_NAME).from("information_schema.tables")
                        .where(field("table_schema").eq(access.db().getDbName()))
                        .fetchSet(TABLE_NAME);
            }
            case SQLITE -> {
                final Field<String> NAME = field("name", TABLE_NAME_TYPE);
                yield dsl.select(NAME).from(table("sqlite_schema"))
                        .where(field("type").eq("table")).and(NAME.notLike("sqlite_%"))
                        .fetchSet(NAME);
            }
            case H2 -> {
                final Field<String> TABLE_NAME = field("table_name", TABLE_NAME_TYPE);
                yield dsl.select(TABLE_NAME).from("information_schema.tables").fetchSet(TABLE_NAME);
            }
        };
        return tableNames.stream().map(String::toUpperCase).collect(Collectors.toSet());
    }

}
