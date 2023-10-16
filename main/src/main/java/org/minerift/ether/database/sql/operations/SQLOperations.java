package org.minerift.ether.database.sql.operations;

import org.jooq.Record;
import org.jooq.*;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.util.reflect.ReflectedMethod;
import org.minerift.ether.util.reflect.ReflectedMethods;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// A collection of SQL operations supported by various dialects
// Intent of the class is to abstract each operation so that it can run under multiple dialects
public class SQLOperations {

    public static <M> void createDatabaseIfNotExists(DSLContext ctx, Model<M> model, boolean async) {
        ReflectedMethod proc = getOpMethodForDialect(CreateDatabaseIfNotExists.class, SQLDialect.H2);
        Query query = proc.invoke(null, ctx, model);
        if(async) {
            query.executeAsync();
        } else {
            query.execute();
        }
    }

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

    public static class CreateDatabaseIfNotExists implements SQLOperation {

        @SQLOpProc
        @SupportedBy(dialects = { SQLDialect.MY_SQL })
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
                    WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'ether')\\gexec
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

    public static class InsertOrUpdate implements SQLOperation {

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

    public interface SQLOperation {
        // empty
    }

}
