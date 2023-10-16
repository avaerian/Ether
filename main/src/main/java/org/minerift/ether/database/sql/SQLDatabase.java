package org.minerift.ether.database.sql;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.SQLOperations;

import javax.sql.DataSource;
import java.util.Set;

public class SQLDatabase {

    private final DSLContext ctx;
    private Set<Model<?>> tables;

    public SQLDatabase(Set<Model<?>> tables, SQLDialect dialect) {
        // TODO: establish connection
        DataSource dataSource = null;
        this.ctx = DSL.using(dataSource, dialect);
        this.tables = tables;
    }

    public void saveAllTables() {
        for(Model<?> model : tables) {
            //SQLOperations
            //model.
        }
    }
}
