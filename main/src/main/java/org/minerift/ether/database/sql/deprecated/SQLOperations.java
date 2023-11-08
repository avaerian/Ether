package org.minerift.ether.database.sql.deprecated;

import org.jooq.Record;
import org.jooq.*;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Fields;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.SupportedBy;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.util.reflect.ReflectedMethod;
import org.minerift.ether.util.reflect.ReflectedMethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.minerift.ether.database.sql.SQLDialect.*;

// A collection of SQL operations supported by various dialects
// Intent of the class is to abstract each operation so that it can run under multiple dialects
@Deprecated
public class SQLOperations {


    // TODO: When creating a database, select a dialect for the database.
    //       - Need to switch to an object-oriented approach for representing these queries more easily
    //       - On database object init, cache all queries for dialect (for single and batched queries, use bind values)
    //       - Each database will contain CachedQueries for use in each db (allows for easier db migrations/switching)



    // CREATE DATABASE IF NOT EXISTS

    public static <M> void createDatabaseIfNotExists(DSLContext ctx, Model<M> model, boolean async) {
        Query query = CreateDatabaseIfNotExists.getQuery(ctx, model);
        if(query != null) {
            if(async) {
                query.executeAsync();
            } else {
                query.execute();
            }
        }
    }

    private static class CreateDatabaseIfNotExists implements SQLOperation {

        public static <M> Query getQuery(DSLContext ctx, Model<M> model) {
            return switch (adapt(ctx.dialect())) {
                case MYSQL      -> proc1(ctx, model);
                case POSTGRES   -> proc2(ctx, model);
                case SQLITE, H2 -> proc3(ctx, model);
            };
        }

        @SQLOpProc
        @SupportedBy(dialects = { SQLDialect.MYSQL})
        private static <M> Query proc1(DSLContext ctx, Model<M> model) {
            return ctx.createDatabaseIfNotExists(model.TABLE_NAME);
        }

        @SQLOpProc
        @SupportedBy(dialects = { SQLDialect.POSTGRES })
        private static <M> Query proc2(DSLContext ctx, Model<M> model) {
            // TODO: test this
            return ctx.query(
                new String("""
                    SELECT 'CREATE DATABASE ether'
                    WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'ether')
                """)
            );
        }

        @SQLOpProc
        @SupportedBy(dialects = { SQLDialect.SQLITE, SQLDialect.H2 })
        private static <M> Query proc3(DSLContext ctx, Model<M> model) {
            // do nothing; database already exists file-based db
            return null;
        }
    }




    // CREATE TABLE IF NOT EXISTS

    public static <M> void createTableIfNotExists(DSLContext ctx, Model<M> model, boolean async) {
        final var query = CreateTableIfNotExists.getQuery(ctx, model);
        if(async) {
            query.executeAsync();
        } else {
            query.execute();
        }
    }

    private static class CreateTableIfNotExists implements SQLOperation {

        public static <M> CreateTableElementListStep getQuery(DSLContext ctx, Model<M> model) {
            // All supported dialects support this query
            return proc1(ctx, model);
        }

        @SQLOpProc
        @SupportedBy(dialects = { SQLDialect.MYSQL, SQLDialect.POSTGRES, SQLDialect.SQLITE, SQLDialect.H2 })
        private static <M> CreateTableElementListStep proc1(DSLContext ctx, Model<M> model) {
            var query = ctx.createTableIfNotExists(model.TABLE)
                    .columns(model.getFields().asSQLFields())
                    .primaryKey(model.getPrimaryKeys().asSQLFields());

            Fields<M, ?> uniqueFields = model.getUniqueFields();
            if(!uniqueFields.isEmpty()) {
                query.unique(model.getUniqueFields().asSQLFields());
            }

            return query;
        }
    }




    // INSERT OR UPDATE

    // Recommended for saving an object while the session is running
    public static <M> void insertOrUpdate(DSLContext ctx, Model<M> model, M obj, boolean async) {
        Query query = InsertOrUpdate.getQuery(ctx, model, obj);
        if(async) {
            query.executeAsync();
        } else {
            query.execute();
        }
    }

    // Recommended for saving small batches of objects while the session is running
    @Deprecated // TODO: needs fixing
    public static <M> void insertOrUpdate(DSLContext ctx, Model<M> model, Collection<M> objs, boolean async) {
        // TODO: batch appears to only execute the last query for some reason
        List<Query> upserts = new ArrayList<>(objs.size());
        objs.forEach(obj -> upserts.add(InsertOrUpdate.getQuery(ctx, model, obj)));
        Batch batch = ctx.batch(upserts);
        if (async) {
            batch.executeAsync();
        } else {
            batch.execute();
        }
    }

    private static class InsertOrUpdate implements SQLOperation {

        public static <M> Query getQuery(DSLContext ctx, Model<M> model, M obj) {
            return switch (adapt(ctx.dialect())) {
                case MYSQL              -> proc1(ctx, model, obj);
                case SQLITE, POSTGRES   -> proc2(ctx, model, obj);
                case H2                 -> proc3(ctx, model, obj);
            };
        }

        @SQLOpProc
        @SupportedBy(dialects = { SQLDialect.MYSQL })
        private static <M> Insert<Record> proc1(DSLContext ctx, Model<M> model, M obj) {
            final var values = model.dumpSQLValuesForObj(obj);
            return ctx.insertInto(model.TABLE)
                    .set(values)
                    .onDuplicateKeyUpdate()
                    .set(values);
        }

        @SQLOpProc
        @SupportedBy(dialects = { SQLITE, POSTGRES })
        private static <M> Insert<Record> proc2(DSLContext ctx, Model<M> model, M obj) {
            return ctx.insertInto(model.TABLE)
                    .set(model.dumpSQLValuesForObj(obj))
                    .onConflict(model.getPrimaryKeys().asSQLFields())
                    .doUpdate()
                    .set(model.dumpSQLValuesForObj(obj, model.getFieldsNoKeys().getArray()));
        }

        @SQLOpProc
        @SupportedBy(dialects = { SQLDialect.H2 })
        private static <M> Merge<Record> proc3(DSLContext ctx, Model<M> model, M obj) {
            // TODO: try SQL MERGE statement for inserting or updating in one call
            //     - Supported by most dialects except MySQL (use INSERT ON DUPLICATE KEY UPDATE for that)
            return ctx.mergeInto(model.TABLE)
                    .using(ctx.selectOne())
                    .on()
                    .whenMatchedThenUpdate()
                    .set(model.dumpSQLValuesForObj(obj, model.getFieldsNoKeys().getArray()))
                    .whenNotMatchedThenInsert()
                    .set(model.dumpSQLValuesForObj(obj));
        }
    }

    public static <M> Result<Record> fetchRecords(DSLContext ctx, Model<M> model, Field<M, ?, ?> ... fields) {
        return ctx.select(Fields.of(model, fields).asSQLFields()).from(model.TABLE).fetch();
    }

    public static <M> Result<Record> fetchRecords(DSLContext ctx, Model<M> model, Condition ... conditions) {
        return ctx.select().from(model.TABLE).where(conditions).fetch();
    }

    public static <M> Result<Record> fetchAll(DSLContext ctx, Model<M> model) {
        // .keepStatement(false) returns a CloseableResultQuery
        return ctx.select().from(model.TABLE).fetch();
    }

    @Deprecated
    private static ReflectedMethod getOpMethodForDialect(Class<? extends SQLOperation> opCls, SQLDialect dialect) {
        final var reflectedOpCls = Reflect.of(opCls);
        ReflectedMethods methods = reflectedOpCls.getMethods().filter(method -> method.hasAnnotation(SQLOpProc.class));
        for(ReflectedMethod method : methods) {
            // This assumes that the SupportedBy annotation will exist for methods annotated with SQLOpProc
            SupportedBy supported = method.getAnnotation(SupportedBy.class);
            for(SQLDialect d : supported.dialects()) {
                if(d == dialect) {
                    return method;
                }
            }
        }
        throw new UnsupportedOperationException("Operation method for " + opCls.getName() + " could not be found for " + dialect);
    }

    private interface SQLOperation {
        // empty
    }
}
