package org.minerift.ether.database.nusql;

import org.minerift.ether.database.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public class NuSQLAccess extends DatabaseAccess implements AutoCloseable {

    private final Connection conn;
    protected boolean committed;

    public NuSQLAccess(NuSQLDatabase db, Connection conn) throws DatabaseException {
        super(db);
        this.conn = conn;
    }

    @Override
    public void commit() throws DatabaseException {
        if(!committed) {
            try {
                conn.commit();
                this.committed = true;
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
        }
    }

    protected void markUncommitted() {
        this.committed = false;
    }

    @Override
    public void rollback() throws DatabaseException {
        try {
            conn.rollback();
            markUncommitted();
        } catch (SQLException e) {
            throw new DatabaseException(e);
        }
    }

    @Override
    public Set<String> getDatabaseTables() {
        return Set.of();
    }

    @Override
    public void createTable(Model<?, ?> model) {

    }

    @Override
    public <MO, PK> int insert(Model<MO, PK> model, MO obj) {
        return 0;
    }

    @Override
    public <MO, PK> int[] insert(Model<MO, PK> model, Collection<MO> objs) {
        return new int[0];
    }

    @Override
    public <MO, PK> int update(Model<MO, PK> model, MO obj) {
        return 0;
    }

    @Override
    public <MO, PK> int[] update(Model<MO, PK> model, Collection<MO> objs) {
        return new int[0];
    }

    @Override
    public <MO, PK> int upsert(Model<MO, PK> model, MO obj) {
        return 0;
    }

    @Override
    public <MO, PK> int[] upsert(Model<MO, PK> model, Collection<MO> objs) {
        return new int[0];
    }

    @Override
    public <MO, PK> int delete(Model<MO, PK> model, MO obj) {
        return 0;
    }

    @Override
    public <MO, PK> int[] delete(Model<MO, PK> model, Collection<MO> objs) {
        return new int[0];
    }

    @Override
    public <MO, PK> int deleteById(Model<MO, PK> model, PK id) {
        return 0;
    }

    @Override
    public <MO, PK> int[] deleteByIds(Model<MO, PK> model, Collection<PK> ids) {
        return new int[0];
    }

    @Override
    public <MO, PK> Result<MO> selectById(Model<MO, PK> model, PK id) {
        return null;
    }

    @Override
    public <MO, PK> Result<MO> selectAll(Model<MO, PK> model) {
        return null;
    }

    @Override
    public <MO, PK> Stream<PK> selectAllIds(Model<MO, PK> model) {
        return Stream.empty();
    }

    @Override
    public void close() throws DatabaseException {

    }
}
