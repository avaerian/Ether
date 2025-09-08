package org.minerift.ether.database.sql.op.ddl;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.SQLDataType;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.sql.SQLAccess;

import java.util.stream.Stream;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

@SuppressWarnings("Duplicates")
public class DDLGetColumns {

    /*
    public static <MO, PK> Set<String> getTableColumnNames(NuSQLAccess access, Model<MO, PK> model) {
        final DataType<String> COL_NAME_TYPE = SQLDataType.VARCHAR;
        final DSLContext dsl = access.dsl();
        Set<String> columns = switch (access.dialect()) {
            case POSTGRES -> {
                final Field<String> COL_NAME = field("column_name", COL_NAME_TYPE);
                yield dsl.select(COL_NAME).from("information_schema.columns")
                        .where(field("table_schema").eq("public"))
                        .and(field("table_type").eq("BASE TABLE"))
                        .fetchSet(COL_NAME);
            }
            case MYSQL -> {
                final Field<String> TABLE_NAME = field("table_name", COL_NAME_TYPE);
                yield dsl.select(TABLE_NAME).from("information_schema.tables")
                        .where(field("table_schema").eq(access.db().getName()))
                        .fetchSet(TABLE_NAME);
            }
            case SQLITE -> {
                final Field<String> NAME = field("name", COL_NAME_TYPE);
                yield dsl.select(NAME).from(table("sqlite_schema"))
                        .where(field("type").eq("table")).and(NAME.notLike("sqlite_%"))
                        .fetchSet(NAME);
            }
            case H2 -> {
                final Field<String> TABLE_NAME = field("table_name", COL_NAME_TYPE);
                yield dsl.select(TABLE_NAME).from(table("INFORMATION_SCHEMA.tables"))
                        .where(field("table_schema").eq("PUBLIC"))
                        .fetchSet(TABLE_NAME);
            }
        };
        return columns;
    }*/

    //public static final String CI_COLLATION = "Latin1_General_CS_IS";

    // Map<String, Field<?, ?, ?>>
    public static <MO, PK> Stream<Record> getTableColumns(SQLAccess access, Model<MO, PK> model) {
        final DSLContext dsl = access.dsl();
        Stream<Record> columns = switch (access.dialect()) {
            case POSTGRES -> {
                dsl.select().from("information_schema.table_constraints")
                        .where(field("table_name").eq(model.getTableName()))
                        .fetchStream()
                        .forEach(System.out::println);


                yield dsl.select().from("information_schema.columns")
                        .where(field("table_schema").eq("public"))
                        .and(field("table_name").equalIgnoreCase(model.getTableName()))
                        .fetchStream();
            }
            case MYSQL -> {
                yield dsl.select().from("information_schema.columns")
                        .where(field("table_schema").eq(access.db().getName()))
                        .and(field("table_name").equalIgnoreCase(model.getTableName()))
                        .fetchStream();
            }
            // Reference: https://www.sqlite.org/pragma.html#pragma_table_info
            case SQLITE -> {
                dsl.resultQuery(String.format("PRAGMA index_list(%s)", model.getTableName())).fetchStream().forEach(System.out::println);
                dsl.select(field("sql", SQLDataType.VARCHAR)).from("sqlite_master")
                        .where(field("tbl_name").eq(model.getTableName()))
                        .fetchStream().forEach(record -> System.out.println(record.get("sql")));

                yield dsl.resultQuery(String.format("PRAGMA table_info(%s)", model.getTableName())).fetchStream();
            }
            case H2 -> {
                yield dsl.select().from(table("INFORMATION_SCHEMA.columns"))
                        .where(field("table_schema").eq("PUBLIC"))
                        .and(field("table_name").equalIgnoreCase(model.getTableName()))
                        .fetchStream();
            }
        };
        columns.forEach(System.out::println);
        return columns;
    }
}
