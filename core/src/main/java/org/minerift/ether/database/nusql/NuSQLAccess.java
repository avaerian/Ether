package org.minerift.ether.database.nusql;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Result;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.DatabaseAccess;
import org.minerift.ether.database.DatabaseException;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.op.ddl.DDLCreateTable;
import org.minerift.ether.database.nusql.op.ddl.DDLGetTables;
import org.minerift.ether.database.nusql.op.bind.NamedBindValues;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public class NuSQLAccess extends DatabaseAccess implements AutoCloseable {
    private final Connection conn;
    private final DSLContext dsl;

    protected boolean committed;

    public NuSQLAccess(NuSQLDatabase db, Connection conn) {
        super(db);
        this.conn = conn;
        this.dsl = db.connConfig.derive(conn).dsl();
        this.committed = false;
    }

    @Override
    public NuSQLDatabase db() {
        return (NuSQLDatabase) db;
    }

    public SQLDialect dialect() {
        return db().getDialect();
    }

    public DSLContext dsl() {
        return dsl;
    }

    @Override
    public void commit() throws DatabaseException {
        try {
            conn.commit();
            this.committed = true;
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to commit", ex);
        }
    }

    @Override
    public void rollback() throws DatabaseException {
        try {
            conn.rollback();
        } catch (SQLException ex) {
            throw new DatabaseException("Failed to rollback", ex);
        }
    }

    private void markUncommitted() { // exists for the purpose of extending behavior if needed / logging
        this.committed = false;
    }

    @Override
    public Set<String> getDatabaseTables() {
        markUncommitted(); // TODO: review
        return DDLGetTables.getDatabaseTables(this);
    }

    @Override
    public void createTable(Model<?, ?> model) {
        markUncommitted(); // TODO: review
        DDLCreateTable.createTableFromModel(this, model);
    }

    @Override
    public <MO, PK> int insert(Model<MO, PK> model, MO obj) {
        markUncommitted();
        return db().insertQuery.createExecutableQuery(this, model, obj).execute();
    }

    @Override
    public <MO, PK> int[] insert(Model<MO, PK> model, Collection<MO> objs) {
        if(objs == null || objs.isEmpty()) {
            return new int[0];
        }
        markUncommitted();
        return db().insertQuery.createExecutableBatch(this, model, objs).execute();
    }

    @Override
    public <MO, PK> int update(Model<MO, PK> model, MO obj) {
        markUncommitted();
        return db().updateQuery.createExecutableQuery(this, model, obj).execute();
    }

    @Override
    public <MO, PK> int[] update(Model<MO, PK> model, Collection<MO> objs) {
        if(objs == null || objs.isEmpty()) {
            return new int[0];
        }
        markUncommitted();
        return db().updateQuery.createExecutableBatch(this, model, objs).execute();
    }

    @Override
    public <MO, PK> int upsert(Model<MO, PK> model, MO obj) {
        markUncommitted();
        return db().upsertQuery.createExecutableQuery(this, model, obj).execute();
    }

    @Override
    public <MO, PK> int[] upsert(Model<MO, PK> model, Collection<MO> objs) {
        if(objs == null || objs.isEmpty()) {
            return new int[0];
        }
        markUncommitted();
        return db().upsertQuery.createExecutableBatch(this, model, objs).execute();
    }

    // TODO: for delete operations, add bool arg for deleting rows in depending tables for foreign fields

    @Override
    public <MO, PK> int delete(Model<MO, PK> model, MO obj) {
        markUncommitted();
        return db().deleteQuery.createExecutableQuery(this, model, obj).execute();
    }

    @Override
    public <MO, PK> int[] delete(Model<MO, PK> model, Collection<MO> objs) {
        if(objs == null || objs.isEmpty()) {
            return new int[0];
        }
        markUncommitted();
        return db().deleteQuery.createExecutableBatch(this, model, objs).execute();
    }

    @Override
    public <MO, PK> int deleteById(Model<MO, PK> model, PK id) {
        markUncommitted();
        return db().deleteQuery.createExecutableQuery(this, model, NamedBindValues.of(model.getPrimaryKey(), id))
                .execute();
    }

    @Override
    public <MO, PK> int[] deleteByIds(Model<MO, PK> model, Collection<PK> ids) {
        if(ids == null || ids.isEmpty()) {
            return new int[0];
        }
        markUncommitted();
        return db().deleteQuery.createExecutableBatchIds(this, model, ids).execute();
    }

    @Override
    public <MO, PK> NuSQLResult<MO> selectById(Model<MO, PK> model, PK id) {
        Result<Record> result = db().selectByIdQuery.createExecutableQuery(
                this, model, NamedBindValues.of(model.getPrimaryKey(), id)).fetch();
        return new NuSQLResult<>(model, result);
    }

    @Override
    public <MO, PK> NuSQLResult<MO> selectAll(Model<MO, PK> model) {
        Result<Record> result = db().selectAllQuery.createExecutableQuery(this, model).fetch();
        return new NuSQLResult<>(model, result);
    }

    @Override
    public <MO, PK> Stream<PK> selectAllIds(Model<MO, PK> model) {
        Result<Record> jooqResult = db().selectAllIdsQuery.createExecutableQuery(this, model).fetch();
        NuSQLResult<MO> result = new NuSQLResult<>(model, jooqResult);
        return result.streamField(model.getPrimaryKey());
    }

    @Override
    public void close() throws SQLException {
        conn.close();
    }
}
