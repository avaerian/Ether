package org.minerift.ether.database.sql;

import org.jooq.BatchBindStep;
import org.jooq.CloseableQuery;
import org.jooq.DSLContext;
import org.minerift.ether.database.nusql.SQLDialect;
import org.minerift.ether.database.nusql.adapters.Adapter;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.op.ddl.DDLCreateTable;
import org.minerift.ether.database.sql.op.ddl.DDLGetTables;
import org.minerift.ether.database.sql.op.dml.bind.NamedBindValues;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public SQLDialect dialect() {
        return db.getDialect();
    }

    public SQLDatabase db() {
        return db;
    }

    // DDL Operations

    public Set<String> getDatabaseTables() {
        return DDLGetTables.getDatabaseTables(this);
    }

    public <M, K> void createTable(Model<M, K> model) {
        DDLCreateTable.createTableFromModel(this, model);
    }


    // DML Operations

    public <M, K> int insert(Class<? extends Model<M, K>> modelClazz, M obj) {
        Model<M, K> model = db.getModel(modelClazz);
        CloseableQuery query = db.INSERT_QUERY.getJooqQuery(this, model, obj);
        return query.execute();
    }

    public <M, K> int[] insert(Class<? extends Model<M, K>> modelClazz, Collection<M> objs) {
        Model<M, K> model = db.getModel(modelClazz);
        BatchBindStep batch = db.INSERT_QUERY.getJooqBatch(this, model, objs);
        return batch.execute();
    }

    public <M, K> int update(Class<? extends Model<M, K>> modelClazz, M obj) {
        Model<M, K> model = db.getModel(modelClazz);
        CloseableQuery query = db.UPDATE_QUERY.getJooqQuery(this, model, obj);
        return query.execute();
    }

    public <M, K> int[] update(Class<? extends Model<M, K>> modelClazz, Collection<M> objs) {
        Model<M, K> model = db.getModel(modelClazz);
        BatchBindStep batch = db.UPDATE_QUERY.getJooqBatch(this, model, objs);
        return batch.execute();
    }

    public <M, K> int insertOrUpdate(Class<? extends Model<M, K>> modelClazz, M obj) {
        Model<M, K> model = db.getModel(modelClazz);
        CloseableQuery query = db.UPSERT_QUERY.getJooqQuery(this, model, obj);
        return query.execute();
    }

    public <M, K> int[] insertOrUpdate(Class<? extends Model<M, K>> modelClazz, Collection<M> objs) {
        Model<M, K> model = db.getModel(modelClazz);
        BatchBindStep batch = db.UPSERT_QUERY.getJooqBatch(this, model, objs);
        return batch.execute();
    }

    public <M, K> int deleteById(Class<? extends Model<M, K>> modelClazz, K id) {
        Model<M, K> model = db.getModel(modelClazz);
        CloseableQuery query = db.DELETE_QUERY.getJooqQuery(this, model, NamedBindValues.of(model.getPrimaryKey(), id));
        return query.execute();
    }

    public <M, K> int[] deleteByIds(Class<? extends Model<M, K>> modelClazz, Collection<K> ids) {
        Model<M, K> model = db.getModel(modelClazz);
        BatchBindStep batch = db.DELETE_QUERY.getJooqBatchIds(this, model, ids);
        return batch.execute();
    }

    public <M, K> int delete(Class<? extends Model<M, K>> modelClazz, M obj) {
        Model<M, K> model = db.getModel(modelClazz);
        CloseableQuery query = db.DELETE_QUERY.getJooqQuery(this, model, obj);
        return query.execute();
    }

    public <M, K> int[] delete(Class<? extends Model<M, K>> modelClazz, Collection<M> objs) {
        Model<M, K> model = db.getModel(modelClazz);
        BatchBindStep batch = db.DELETE_QUERY.getJooqBatch(this, model, objs);
        return batch.execute();
    }

    public <M, K> SQLResult<M> selectById(Class<? extends Model<M, K>> modelClazz, K id) {
        Model<M, K> model = db.getModel(modelClazz);
        var query = db.SELECT_ID_QUERY.getJooqQuery(this, model, NamedBindValues.of(model.getPrimaryKey(), id));
        return new SQLResult<>(model, query.fetch());
    }

    public <M, K> SQLResult<M> selectAll(Class<? extends Model<M, K>> modelClazz) {
        Model<M, K> model = db.getModel(modelClazz);
        var query = db.SELECT_ALL_QUERY.getJooqQuery(this, model);
        return new SQLResult<>(model, query.fetch());
    }

    public <M, K> Stream<K> selectAllIdsStream(Class<? extends Model<M, K>> modelClazz) {
        Model<M, K> model = db.getModel(modelClazz);
        var query = db.SELECT_ALL_IDS_QUERY.getJooqQuery(this, model);

        // Get result and read ids
        SQLResult<M> result = new SQLResult<>(model, query.fetch());
        return result.stream().map(record -> result.readField(model.getPrimaryKey(), record));
    }

    public <M, K> Set<K> selectAllIds(Class<? extends Model<M, K>> modelClazz) {
        return selectAllIdsStream(modelClazz).collect(Collectors.toSet());
    }

    // Used for mapping to complex field data type
    public <M, K, R> Set<R> selectAllIds(Class<? extends Model<M, K>> modelClazz, Adapter<R, K> adapter) {
        return selectAllIdsStream(modelClazz).map(adapter::adaptFrom).collect(Collectors.toSet());
    }

    public void commit() throws SQLException {
        conn.commit();
    }

    public void rollback() throws SQLException {
        conn.rollback();
    }

    public boolean getAutoCommit() throws SQLException {
        return conn.getAutoCommit();
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}
