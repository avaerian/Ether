package org.minerift.ether.database.nusql.op.dml;

import org.minerift.ether.database.Field;
import org.minerift.ether.database.Fields;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.*;

import static org.jooq.impl.DSL.condition;
import static org.minerift.ether.database.nusql.SQLUtils.*;

public class NuDMLUpsert extends NuDMLOp {
    public NuDMLUpsert(SQLDatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected <OBJ, PK> RawModelQuery createQueryForModel(SQLDatabaseCreationContext ctx, Model<OBJ, PK> model) {
        Fields<OBJ> allFields = model.getFields();
        Fields<OBJ> partFields = model.getFields().exclude(model.getPrimaryKey());

        return switch (ctx.dialect()) {
            case MYSQL -> {
                String sql = ctx.dsl().insertInto(asJooqTable(model))
                        .set(getNullBindValues(allFields))
                        .onDuplicateKeyUpdate()
                        .set(getNullBindValues(partFields))
                        .getSQL();
                yield new RawModelQuery(sql, allFields.getNames(), partFields.getNames());
            }

            case SQLITE, POSTGRES -> {
                String sql = ctx.dsl().insertInto(asJooqTable(model))
                        .set(getNullBindValues(allFields))
                        .onConflict(asJooqField(model.getPrimaryKey()))
                        .doUpdate()
                        .set(getNullBindValues(partFields))
                        .getSQL();
                yield new RawModelQuery(sql, allFields.getNames(), partFields.getNames());
            }

            case H2 -> {
                Field<OBJ, PK, ?> primaryKey = model.getPrimaryKey();
                String sql = ctx.dsl().mergeInto(asJooqTable(model))
                        .using(ctx.dsl().selectOne())
                        .on(condition(getNullBindValue(primaryKey)))
                        .whenMatchedThenUpdate()
                        .set(getNullBindValues(partFields))
                        .whenNotMatchedThenInsert()
                        .set(getNullBindValues(allFields))
                        .getSQL();
                yield new RawModelQuery(sql, new String[] { primaryKey.getName() }, partFields.getNames(), allFields.getNames());
            }
        };
    }
}
