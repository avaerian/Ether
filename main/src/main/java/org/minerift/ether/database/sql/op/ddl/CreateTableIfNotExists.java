package org.minerift.ether.database.sql.op.ddl;

import org.jooq.CreateTableElementListStep;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.database.sql.model.Fields;
import org.minerift.ether.database.sql.model.Model;

@Deprecated
public class CreateTableIfNotExists {

    /*
    public CreateTableIfNotExists(SQLContext ctx) {
        super(ctx);
    }

    public <M> CreateTableElementListStep getQuery(Model<M> model) {
        // All supported dialects support this query
        return proc1(model);
    }

    @SupportedBy(dialects = { SQLDialect.MYSQL, SQLDialect.POSTGRES, SQLDialect.SQLITE, SQLDialect.H2 })
    private <M> CreateTableElementListStep proc1(Model<M> model) {
        var query = ctx.dsl().createTableIfNotExists(model.asJooqTable())
                .columns(model.getFields().asSQLFields())
                .primaryKey(model.getPrimaryKey().asJooqField());

        Fields<M, ?> uniqueFields = model.getUniqueFields();
        if(!uniqueFields.isEmpty()) {
            query.unique(uniqueFields.asSQLFields());
        }

        return query;
    }
    */
}
