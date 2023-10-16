package org.minerift.ether.database.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandGrid;
import org.minerift.ether.user.EtherUser;

import java.sql.SQLException;
import java.util.*;

import static org.jooq.impl.DSL.*;
import static org.minerift.ether.database.sql.IslandModel.ISLAND_MODEL;

// Playground class for messing around and figuring out database stuffs
public class DatabasePlayground {

    // TODO: create metadata database for plugin/server/db state

    public static void main(String[] args) {

        final HikariDataSource dataSource = establishConnection();
        // TODO: important to read: https://www.jooq.org/doc/latest/manual/sql-building/dsl-context/connection-vs-datasource/
        final DSLContext ctx = DSL.using(dataSource, SQLDialect.H2);

        // No need to create database for SQLite, H2

        // Creating a database like this is not supported by SQLite; this is done simply by opening the file
        // For MariaDB, MySQL
        //ctx.createDatabaseIfNotExists("ether").execute();

        // For PostgreSQL
        // TODO: test this
        /*
        ctx.execute(new String("""
                SELECT 'CREATE DATABASE ether'
                WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ether')\\gexec
        """));
        */

        ctx.createTableIfNotExists("users")
                .column("uuid", SQLDataType.UUID)
                .primaryKey("uuid")
                .column("island_id", SQLDataType.INTEGER)
                .column("island_role", SQLDataType.VARCHAR(64))
                .execute();

        ctx.createTableIfNotExists(ISLAND_MODEL.TABLE)
                .column(ISLAND_MODEL.ID.getSQLField())
                .primaryKey(ISLAND_MODEL.ID.getSQLField())

                .column(ISLAND_MODEL.IS_DELETED.getSQLField())
                //.column(ISLAND_MODEL.MEMBERS.getField())
                //.column("is_deleted", SQLDataType.BIT)
                //.column("members", SQLDataType.UUID.array())
                .column("bottom_left_bound", SQLDataType.BIGINT)
                .column("top_right_bound", SQLDataType.BIGINT)
                .execute();

        // TODO: create API that can easily insert or update records
        //  - When saving data to the database, first query all of the ids saved in the database into a list
        //  - With the queried ids, update records
        //  - For the rest of the ids, insert records
        //  - Benchmark this compared to onDuplicateKeyUpdate()
        ctx.insertInto(table("users"))
                // Insert new record with specified values
                .set(field("uuid"), UUID.fromString("e51f2acb-1013-4b8c-9841-6b7d6c498ea7"))
                .set(field("island_id"), new Random().nextInt(421))
                .onDuplicateKeyUpdate()
                // If duplicate key already exists, update record with specified values
                .set(field("island_id"), 353)
                .set(field("island_role", SQLDataType.VARCHAR(64)), "OWNER")
                .execute();

        IslandGrid grid = new IslandGrid();
        final int count = 100;
        final EtherUser user = EtherUser.builder().setUUID(UUID.randomUUID()).build();

        for(int i = 0; i < count; i++) {
            Island island = Island.builder()
                    .setTile(grid.getNextTile(), true)
                    .setDeleted(false)
                    .setOwner(user)
                    .build();
            grid.registerIsland(island);
        }

        saveObjsToTable(ctx, ISLAND_MODEL, grid.getIslandsView(), false);

        // TODO: for API:
        //  - Database<T> ->
        //  - Model<T> -> represents a database model of the object (table, fields, etc.)

        Result<Record> result = ctx.select().from(ISLAND_MODEL.TABLE).fetch();
        for(Record record : result) {
            System.out.println(record);
        }

        System.out.println("Primary keys:");
        System.out.println(Arrays.stream(ISLAND_MODEL.getPrimaryKeys().getArray()).map(field -> field.getSQLField().getName()).toList());

        dataSource.close();
    }

    private <M> void createDatabaseIfNotExists(DSLContext ctx, Model<M> model, boolean async) {
        Query query = null;
        switch (ctx.dialect()) {
            case MYSQL, MARIADB, YUGABYTEDB:
                query = ctx.createDatabaseIfNotExists(model.TABLE_NAME);
                break;
            case POSTGRES:
                // TODO: test this
                query = ctx.query(new String("""
                                SELECT 'CREATE DATABASE ether'
                                WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ether')\\gexec
                        """));
                break;
            case SQLITE, H2:
                break;
            default:
                throw new UnsupportedOperationException("Unsupported database");
        }
        if(query != null) {
            if(async) {
                query.executeAsync();
            } else {
                query.execute();
            }
        }
    }

    private static <M> void createTableIfNotExists(DSLContext ctx, Model<M> model, boolean async) {
        final var query = ctx.createTableIfNotExists(model.TABLE)
                .columns(model.getFields().asSQLFields())
                .primaryKey(model.getPrimaryKeys().asSQLFields());
        // TODO: this pattern will be removed and the methods will
        //       be refactored into sync and async for better handling
        if(async) {
           query.executeAsync();
        } else {
           query.execute();
        }
    }

    // TODO: For creating multiple methods for supporting multiple dialects,
    //       an annotation could be created indicating the dialects supported
    //       for those operations. Then, these methods could be reflectively
    //       mapped into a hashmap and called based on the dialect.
    //       SQLOperations.class would contain all of the operation calls to support the appropriate dialects,
    //       with public methods that call the supported operation methods based on dialect.

    private static <M> Insert<Record> saveObjToTableStmt(DSLContext ctx, Model<M> model, M obj) {
        final var values = model.dumpSQLValuesForObj(obj);
        // TODO: try SQL MERGE statement for inserting or updating in one call
        //     - Supported by most dialects except MySQL (use INSERT ON DUPLICATE KEY UPDATE for that)
        //ctx.mergeInto(null)
        /*
        ctx.mergeInto(model.TABLE)
                .using(ctx.selectOne())
                .on()
                .whenMatchedThenUpdate()
                .set(values)
                .whenNotMatchedThenInsert()
                .set(values)
                .execute();
        */



        return ctx.insertInto(model.TABLE)
                .set(values)
                .onDuplicateKeyUpdate()
                .set(values);
    }

    private static <M> void saveObjToTable(DSLContext ctx, Model<M> model, M obj, boolean async) {
        Query query = saveObjToTableStmt(ctx, model, obj);
        if(async) {
            query.executeAsync();
        } else {
            query.execute();
        }
    }

    private static <M> void saveObjsToTable(DSLContext ctx, Model<M> model, Collection<M> objs, boolean async) {
        List<Query> inserts = new ArrayList<>(objs.size());
        objs.forEach(obj -> inserts.add(saveObjToTableStmt(ctx, model, obj)));
        Batch batch = ctx.batch(inserts);
        if(async) {
            batch.executeAsync();
        } else {
            batch.execute();
        }
    }

    private static <M> Result<Record> fetchRecords(DSLContext ctx, Model<M> model, org.minerift.ether.database.sql.model.Field<M, ?>... fields) {
        return ctx.select(
                org.minerift.ether.database.sql.model.Fields.of(model, fields).asSQLFields()
        ).from(model.TABLE).fetch();
    }

    private static <M> Result<Record> fetchRecords(DSLContext ctx, Model<M> model, Condition ... conditions) {
        return ctx.select().from(model.TABLE).where(conditions).fetch();
    }

    private static <M> Result<Record> fetchAll(DSLContext ctx, Model<M> model) {
        return ctx.select().from(model.TABLE).fetch();
    }

    private static HikariDataSource establishConnection() {
        final HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:~/test.db");
        config.setUsername("root");
        config.setPassword("");
        config.addDataSourceProperty("cachePrepStmts", "true");

        HikariDataSource dataSource = new HikariDataSource(config);

        try {
            if(!dataSource.getConnection().isValid(5 * 1000)) { // 5 seconds
                System.out.println("Database connection was unable to be established :(");
                return null;
            }

            System.out.println("Database connection has been established!");
            return dataSource;

        } catch (SQLException ex) {
            // TODO: handle better
            throw new RuntimeException(ex);
        }
    }
}
