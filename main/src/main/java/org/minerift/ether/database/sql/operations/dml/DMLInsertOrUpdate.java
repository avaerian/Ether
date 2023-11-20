package org.minerift.ether.database.sql.operations.dml;

import org.jooq.CloseableQuery;
import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.SupportedBy;
import org.minerift.ether.database.sql.operations.dml.cache.PerModelBindQueryCache;
import org.minerift.ether.database.sql.operations.dml.cache.RawBindQuery;
import org.minerift.ether.database.sql.operations.dml.query.DMLQuery;

import static org.jooq.impl.DSL.condition;
import static org.minerift.ether.database.sql.SQLDialect.*;

public class DMLInsertOrUpdate extends PerModelDMLOp<RawBindQuery, DMLQuery> implements Batchable {

    public DMLInsertOrUpdate(SQLContext ctx) {
        super(ctx, new PerModelBindQueryCache());
    }

    @Override
    protected RawBindQuery getQueryForCache(Model<?> model) {
        return switch (ctx.getDialect()) {
            case MYSQL              -> cacheProc1(model);
            case SQLITE, POSTGRES   -> cacheProc2(model);
            case H2                 -> cacheProc3(model);
        };
    }

    @SupportedBy(dialects = { MYSQL })
    private <M> RawBindQuery cacheProc1(Model<M> model) {
        var allEmptyFields  = model.getEmptyBindValues();
        var partEmptyFields = model.getEmptyBindValues(model.getFieldsNoKey());

        String sql = ctx.dsl().insertInto(model.TABLE)
                .set(allEmptyFields)
                .onDuplicateKeyUpdate()
                .set(partEmptyFields)
                .getSQL();

        // BIND ORDER -> all fields, part fields
        return new RawBindQuery(sql, allEmptyFields.keySet(), partEmptyFields.keySet());
    }

    @SupportedBy(dialects = { SQLITE, POSTGRES })
    private <M> RawBindQuery cacheProc2(Model<M> model) {
        var allEmptyFields  = model.getEmptyBindValues();
        var partEmptyFields = model.getEmptyBindValues(model.getFieldsNoKey());

        String sql = ctx.dsl().insertInto(model.TABLE)
                .set(allEmptyFields)
                .onConflict(model.getPrimaryKey().getSQLField())
                .doUpdate()
                .set(partEmptyFields)
                .getSQL();

        return new RawBindQuery(sql, allEmptyFields.keySet(), partEmptyFields.keySet());
    }

    @SupportedBy(dialects = { H2 })
    private <M> RawBindQuery cacheProc3(Model<M> model) {
        var keyBindVals = model.getEmptyBindValues(model.getPrimaryKey());
        var allBindVals = model.getEmptyBindValues();
        var partBindVals = model.getEmptyBindValues(model.getFieldsNoKey());

        String sql = ctx.dsl().mergeInto(model.TABLE)
                .using(ctx.dsl().selectOne())
                .on(condition(keyBindVals))
                .whenMatchedThenUpdate()
                .set(partBindVals)
                .whenNotMatchedThenInsert()
                .set(allBindVals)
                .getSQL();

        return new RawBindQuery(sql, keyBindVals.keySet(), partBindVals.keySet(), allBindVals.keySet());
    }

    @Override
    public <M> DMLQuery queryFor(Model<M> model) {
        RawBindQuery rawBindQuery = queryCache.getQuery(model);
        CloseableQuery query = ctx.dsl().query(rawBindQuery.getSQL(), new Object[rawBindQuery.getBindOrder().length]).keepStatement(false);
        System.out.println(query.getBindValues());
        return new DMLQuery(query, rawBindQuery.getBindOrder());
    }

    @Override
    public <M> DMLModelBatch<M> batchFor(Model<M> model) {
        var query = queryFor(model);
        return new DMLModelBatch<>(ctx, model, query.getQuery(), query.getBindOrder());
    }
}
