package org.minerift.ether.database.sql;

import org.jooq.BatchBindStep;
import org.jooq.CloseableQuery;
import org.jooq.DSLContext;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

// Provides access to SQL operations from a connection
public class SQLAccess implements AutoCloseable {

    private final SQLDatabase db;
    private final Connection conn;
    private final DSLContext dsl;

    public SQLAccess(SQLDatabase db, Connection conn) {
        this.db = db;
        this.conn = conn;
        this.dsl = db.connConfig.derive(conn).dsl();
        //this.dsl = DSL.using(conn, db.getDialect().asJooqDialect());
    }

    public DSLContext dsl() {
        return dsl;
    }

    public <M, K> int insert(Class<? extends Model<M, K>> modelClazz, M obj) {
        Model<M, K> model = db.getTable(modelClazz);
        CloseableQuery query = db.INSERT_QUERY.getJooqQuery(this, model, obj);
        return query.execute();
    }

    public <M, K> int[] insert(Class<? extends Model<M, K>> modelClazz, Collection<M> objs) {
        Model<M, K> model = db.getTable(modelClazz);
        BatchBindStep batch = db.INSERT_QUERY.getJooqBatch(this, model, objs);
        return batch.execute();
    }

    public <M, K> int update(Class<? extends Model<M, K>> modelClazz, M obj) {
        Model<M, K> model = db.getTable(modelClazz);
        CloseableQuery query = db.UPDATE_QUERY.getJooqQuery(this, model, obj);
        return query.execute();
    }

    public <M, K> int[] update(Class<? extends Model<M, K>> modelClazz, Collection<M> objs) {
        Model<M, K> model = db.getTable(modelClazz);
        BatchBindStep batch = db.UPDATE_QUERY.getJooqBatch(this, model, objs);
        return batch.execute();
    }

    public <M, K> int insertOrUpdate(Class<? extends Model<M, K>> modelClazz, M obj) {
        Model<M, K> model = db.getTable(modelClazz);
        CloseableQuery query = db.UPSERT_QUERY.getJooqQuery(this, model, obj);
        return query.execute();
    }

    public <M, K> int[] insertOrUpdate(Class<? extends Model<M, K>> modelClazz, Collection<M> objs) {
        Model<M, K> model = db.getTable(modelClazz);
        BatchBindStep batch = db.UPSERT_QUERY.getJooqBatch(this, model, objs);
        return batch.execute();
    }

    public <M, K> int deleteById(Class<? extends Model<M, K>> modelClazz, K id) {
        Model<M, K> model = db.getTable(modelClazz);
        CloseableQuery query = db.DELETE_QUERY.getJooqQuery(this, model, NamedBindValues.of(model.getPrimaryKey(), id));
        return query.execute();
    }

    public <M, K> int delete(Class<? extends Model<M, K>> modelClazz, M obj) {
        Model<M, K> model = db.getTable(modelClazz);
        CloseableQuery query = db.DELETE_QUERY.getJooqQuery(this, model, obj);
        return query.execute();
    }

    public <M, K> int[] delete(Class<? extends Model<M, K>> modelClazz, Collection<M> objs) {
        Model<M, K> model = db.getTable(modelClazz);
        BatchBindStep batch = db.DELETE_QUERY.getJooqBatch(this, model, objs);
        return batch.execute();
    }

    public <M, K> SQLResult<M> selectById(Class<? extends Model<M, K>> modelClazz, K id) {
        Model<M, K> model = db.getTable(modelClazz);
        var query = db.SELECT_ID_QUERY.getJooqQuery(this, model, NamedBindValues.of(model.getPrimaryKey(), id));
        return new SQLResult<>(model, query.fetch());
    }

    public <M, K> SQLResult<M> selectAll(Class<? extends Model<M, K>> modelClazz) {
        Model<M, K> model = db.getTable(modelClazz);
        var query = db.SELECT_ALL_QUERY.getJooqQuery(this, model);
        return new SQLResult<>(model, query.fetch());
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void rollback() throws SQLException {
        conn.rollback();
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}
