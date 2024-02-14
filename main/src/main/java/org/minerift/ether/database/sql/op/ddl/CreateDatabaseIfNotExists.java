package org.minerift.ether.database.sql.op.ddl;

import org.jooq.Query;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.SQLDialect;

@Deprecated
public class CreateDatabaseIfNotExists {

    // TODO: this will be handled individually when initing/connecting to database

    /*
    public CreateDatabaseIfNotExists(SQLContext ctx) {
        super(ctx);
    }

    public Query getQuery(SQLDatabase db) {
        return switch(ctx.getDialect()) {
            case MYSQL      -> proc1(db);
            case POSTGRES   -> proc2(db);
            case SQLITE, H2 -> null; // do nothing; file-based db already exists
        };
    }

    @SupportedBy(dialects = { SQLDialect.MYSQL })
    private Query proc1(SQLDatabase db) {
        return ctx.dsl().createDatabaseIfNotExists(db.getDbName());
        // ctx.dsl().query("use " + db.getDbName()).execute();
    }

    @SupportedBy(dialects = { SQLDialect.POSTGRES })
    private Query proc2(SQLDatabase db) {
        return ctx.dsl().query(
                String.format("""
                    SELECT 'CREATE DATABASE %s'
                    WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = '%s')
                """, db.getDbName(), db.getDbName())
        );
    }
    */
}
