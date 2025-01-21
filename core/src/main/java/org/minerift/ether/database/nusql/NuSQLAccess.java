package org.minerift.ether.database.nusql;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.op.ddl.DDLCreateTable;
import org.minerift.ether.database.nusql.op.ddl.DDLGetTables;
import org.minerift.ether.database.nusql.op.bind.NamedBindValues;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public class NuSQLAccess implements AutoCloseable {

    private final NuSQLDatabase db;
    private final Connection conn;
    private final DSLContext dsl;

    protected boolean committed;

    public NuSQLAccess(NuSQLDatabase db, Connection conn) {
        this.db = db;
        this.conn = conn;
        this.dsl = db.connConfig.derive(conn).dsl();
        this.committed = false;
    }

    public NuSQLDatabase db() {
        return db;
    }

    public SQLDialect dialect() {
        return db.getDialect();
    }

    public DSLContext dsl() {
        return dsl;
    }

    public void commit() throws SQLException {
        conn.commit();
        this.committed = true;
    }

    public void rollback() throws SQLException {
        conn.rollback();
    }

    private void markUncommitted() {
        this.committed = false;
    }

    public Set<String> getDatabaseTables() {
        return DDLGetTables.getDatabaseTables(this);
    }

    public void createTable(Model<?, ?> model) {
        DDLCreateTable.createTableFromModel(this, model);
    }

    public <MO, PK> int insert(Model<MO, PK> model, MO obj) {
        markUncommitted();
        return db.insertQuery.createExecutableQuery(this, model, obj).execute();
    }

    public <MO, PK> int[] insert(Model<MO, PK> model, Collection<MO> objs) {
        if(objs == null || objs.isEmpty()) {
            return new int[0];
        }
        markUncommitted();
        return db.insertQuery.createExecutableBatch(this, model, objs).execute();
    }

    public <MO, PK> int update(Model<MO, PK> model, MO obj) {
        markUncommitted();
        return db.updateQuery.createExecutableQuery(this, model, obj).execute();
    }

    public <MO, PK> int[] update(Model<MO, PK> model, Collection<MO> objs) {
        if(objs == null || objs.isEmpty()) {
            return new int[0];
        }
        markUncommitted();
        return db.updateQuery.createExecutableBatch(this, model, objs).execute();
    }

    public <MO, PK> int upsert(Model<MO, PK> model, MO obj) {
        markUncommitted();
        return db.upsertQuery.createExecutableQuery(this, model, obj).execute();
    }

    public <MO, PK> int[] upsert(Model<MO, PK> model, Collection<MO> objs) {
        if(objs == null || objs.isEmpty()) {
            return new int[0];
        }
        markUncommitted();
        return db.upsertQuery.createExecutableBatch(this, model, objs).execute();
    }

    // TODO: for delete operations, add bool arg for deleting rows in depending tables for foreign fields

    public <MO, PK> int delete(Model<MO, PK> model, MO obj) {
        markUncommitted();
        return db.deleteQuery.createExecutableQuery(this, model, obj).execute();
    }

    public <MO, PK> int[] delete(Model<MO, PK> model, Collection<MO> objs) {
        if(objs == null || objs.isEmpty()) {
            return new int[0];
        }
        markUncommitted();
        return db.deleteQuery.createExecutableBatch(this, model, objs).execute();
    }

    public <MO, PK> int deleteById(Model<MO, PK> model, PK id) {
        markUncommitted();
        return db.deleteQuery.createExecutableQuery(this, model, NamedBindValues.of(model.getPrimaryKey(), id))
                .execute();
    }

    public <MO, PK> int[] deleteByIds(Model<MO, PK> model, Collection<PK> ids) {
        if(ids == null || ids.isEmpty()) {
            return new int[0];
        }
        markUncommitted();
        return db.deleteQuery.createExecutableBatchIds(this, model, ids).execute();
    }

    public <MO, PK> NuSQLResult<MO> selectById(Model<MO, PK> model, PK id) {
        Result<Record> result = db.selectByIdQuery.createExecutableQuery(
                this, model, NamedBindValues.of(model.getPrimaryKey(), id)).fetch();
        return new NuSQLResult<>(model, result);
    }

    public <MO, PK> NuSQLResult<MO> selectAll(Model<MO, PK> model) {
        Result<Record> result = db.selectAllQuery.createExecutableQuery(this, model).fetch();
        return new NuSQLResult<>(model, result);
    }

    public <MO, PK> Stream<PK> selectAllIds(Model<MO, PK> model) {
        Result<Record> jooqResult = db.selectAllIdsQuery.createExecutableQuery(this, model).fetch();
        NuSQLResult<MO> result = new NuSQLResult<>(model, jooqResult);
        return result.streamField(model.getPrimaryKey());
    }



    // Overloads to clean up API for definitions above
    // Parameters here are extremely ugly, but are necessary in-practice
    public <MO, PK> int insert(Class<? extends Model<MO, PK>> modelClazz, MO obj) {
        return insert(db.getModel(modelClazz), obj);
    }

    public <MO, PK> int[] insert(Class<? extends Model<MO, PK>> modelClazz, Collection<MO> objs) {
        return insert(db.getModel(modelClazz), objs);
    }

    public <MO, PK> int update(Class<? extends Model<MO, PK>> modelClazz, MO obj) {
        return update(db.getModel(modelClazz), obj);
    }

    public <MO, PK> int[] update(Class<? extends Model<MO, PK>> modelClazz, Collection<MO> objs) {
        return update(db.getModel(modelClazz), objs);
    }

    public <MO, PK> int upsert(Class<? extends Model<MO, PK>> modelClazz, MO obj) {
        return upsert(db.getModel(modelClazz), obj);
    }

    public <MO, PK> int[] upsert(Class<? extends Model<MO, PK>> modelClazz, Collection<MO> objs) {
        return upsert(db.getModel(modelClazz), objs);
    }

    public <MO, PK> int delete(Class<? extends Model<MO, PK>> modelClazz, MO obj) {
        return delete(db.getModel(modelClazz), obj);
    }

    public <MO, PK> int[] delete(Class<? extends Model<MO, PK>> modelClazz, Collection<MO> objs) {
        return delete(db.getModel(modelClazz), objs);
    }

    public <MO, PK> int deleteById(Class<? extends Model<MO, PK>> modelClazz, PK id) {
        return deleteById(db.getModel(modelClazz), id);
    }

    public <MO, PK> int[] deleteByIds(Class<? extends Model<MO, PK>> modelClazz, Collection<PK> ids) {
        return deleteByIds(db.getModel(modelClazz), ids);
    }

    public <MO, PK> NuSQLResult<MO> selectById(Class<? extends Model<MO, PK>> modelClazz, PK id) {
        return selectById(db.getModel(modelClazz), id);
    }

    public <MO, PK> NuSQLResult<MO> selectAll(Class<? extends Model<MO, PK>> modelClazz) {
        return selectAll(db.getModel(modelClazz));
    }

    public <MO, PK> Stream<PK> selectAllIds(Class<? extends Model<MO, PK>> modelClazz) {
        return selectAllIds(db.getModel(modelClazz));
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}
