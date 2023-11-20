package org.minerift.ether.database.sql;

import com.google.common.base.Preconditions;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.dml.bind.NamedBindValues;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

// M - model
// T - model obj type
// K - key
public class Dao<M extends Model<T>, T, K> {

    private final SQLContext ctx;
    private final M model;
    private final Field<T, K, ?> id;

    public Dao(Class<M> modelClazz, Function<M, Field<T, K, ?>> id, SQLContext ctx) {
        this.ctx = ctx;
        this.model = ctx.getTable(modelClazz);
        this.id = id.apply(model);
        Preconditions.checkArgument(id == model.getPrimaryKey(), "Id must be the primary key of model for Dao!");
    }

    // TODO
    public T fetch(K key) {
        ctx.selectQuery
                .queryFor(model)
                .bind(NamedBindValues.of(id.getName(), id.readJavaAsSQLValue(key)))
                .fetch();
        return null;
    }

    // TODO
    public List<T> fetchAll() {
        ctx.selectAllQuery.queryFor(model).fetch();
        return null;
    }

    public int insertOrUpdate(T obj) {
        return ctx.upsertQuery
                .queryFor(model)
                .bind(model.dumpNamedBindValues_New(obj))
                .execute();
    }

    public int[] insertOrUpdate(Collection<T> objs) {
        return ctx.upsertQuery
                .batchFor(model)
                .bindAll(objs)
                .execute();
    }

    public int insert(T obj) {
        return ctx.insertQuery
                .queryFor(model)
                .bind(model.dumpNamedBindValues_New(obj))
                .execute();
    }

    public int[] insert(Collection<T> objs) {
        return ctx.insertQuery
                .batchFor(model)
                .bindAll(objs)
                .execute();
    }

    public int update(T obj) {
        return ctx.updateQuery
                .queryFor(model)
                .bind(model.dumpNamedBindValues_New(obj))
                .execute();
    }

    public int[] update(Collection<T> obj) {
        return ctx.updateQuery
                .batchFor(model)
                .bindAll(obj)
                .execute();
    }

}
