package org.minerift.ether.database;

import org.minerift.ether.database.nusql.NuSQLResult;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

public abstract class DatabaseAccess implements AutoCloseable {

    protected final Database db;

    public DatabaseAccess(Database db) {
        this.db = db;
    }

    public Database db() {
        return db;
    }

    public <M extends Model<?, ?>> M getModel(Class<M> modelClazz) {
        return db.getModel(modelClazz);
    }

    public abstract void commit() throws DatabaseException;
    public abstract void rollback() throws DatabaseException;

    public abstract Set<String> getDatabaseTables();
    public abstract void createTable(Model<?, ?> model);

    public abstract <MO, PK> int insert(Model<MO, PK> model, MO obj);
    public abstract <MO, PK> int[] insert(Model<MO, PK> model, Collection<MO> objs);

    public abstract <MO, PK> int update(Model<MO, PK> model, MO obj);
    public abstract <MO, PK> int[] update(Model<MO, PK> model, Collection<MO> objs);

    public abstract <MO, PK> int upsert(Model<MO, PK> model, MO obj);
    public abstract  <MO, PK> int[] upsert(Model<MO, PK> model, Collection<MO> objs);

    // TODO: for delete operations, add bool arg overload for deleting rows in depending tables for foreign fields

    public abstract <MO, PK> int delete(Model<MO, PK> model, MO obj);
    public abstract <MO, PK> int[] delete(Model<MO, PK> model, Collection<MO> objs);

    public abstract <MO, PK> int deleteById(Model<MO, PK> model, PK id);
    public abstract <MO, PK> int[] deleteByIds(Model<MO, PK> model, Collection<PK> ids);

    public abstract <MO, PK> Result<MO> selectById(Model<MO, PK> model, PK id);
    public abstract <MO, PK> Result<MO> selectAll(Model<MO, PK> model);
    public abstract <MO, PK> Stream<PK> selectAllIds(Model<MO, PK> model);


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

    public <MO, PK> Result<MO> selectById(Class<? extends Model<MO, PK>> modelClazz, PK id) {
        return selectById(db.getModel(modelClazz), id);
    }

    public <MO, PK> Result<MO> selectAll(Class<? extends Model<MO, PK>> modelClazz) {
        return selectAll(db.getModel(modelClazz));
    }

    public <MO, PK> Stream<PK> selectAllIds(Class<? extends Model<MO, PK>> modelClazz) {
        return selectAllIds(db.getModel(modelClazz));
    }
}
