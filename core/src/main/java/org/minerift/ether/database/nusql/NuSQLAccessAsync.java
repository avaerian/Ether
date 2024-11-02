package org.minerift.ether.database.nusql;

import org.minerift.ether.debug.Debug;

@Debug
@Deprecated
public class NuSQLAccessAsync {

    /*public <MO, PK> CompletableFuture<Integer> insert(Model<MO, PK> model, MO obj) {
        return db.insertQuery.createExecutableQuery(this, model, obj).executeAsync().toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<int[]> insert(Model<MO, PK> model, Collection<MO> objs) {
        return db.insertQuery.createExecutableBatch(this, model, objs).executeAsync().toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<Integer> update(Model<MO, PK> model, MO obj) {
        return db.updateQuery.createExecutableQuery(this, model, obj).executeAsync().toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<int[]> update(Model<MO, PK> model, Collection<MO> objs) {
        return db.updateQuery.createExecutableBatch(this, model, objs).executeAsync().toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<Integer> upsert(Model<MO, PK> model, MO obj) {
        return db.upsertQuery.createExecutableQuery(this, model, obj).executeAsync().toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<int[]> upsert(Model<MO, PK> model, Collection<MO> objs) {
        return db.upsertQuery.createExecutableBatch(this, model, objs).executeAsync().toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<Integer> delete(Model<MO, PK> model, MO obj) {
        return db.deleteQuery.createExecutableQuery(this, model, obj).executeAsync().toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<int[]> delete(Model<MO, PK> model, Collection<MO> objs) {
        return db.deleteQuery.createExecutableBatch(this, model, objs).executeAsync().toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<Integer> deleteById(Model<MO, PK> model, PK id) {
        return db.deleteQuery.createExecutableQuery(this, model, NamedBindValues.of(model.getPrimaryKey(), id))
                .executeAsync().toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<int[]> deleteByIds(Model<MO, PK> model, Collection<PK> ids) {
        return db.deleteQuery.createExecutableBatchIds(this, model, ids).executeAsync().toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<NuSQLResult<MO>> selectById(Model<MO, PK> model, PK id) {
        return db.selectByIdQuery.createExecutableQuery(this, model, NamedBindValues.of(model.getPrimaryKey(), id))
                .fetchAsync()
                .thenApply(result -> new NuSQLResult<>(model, result))
                .toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<NuSQLResult<MO>> selectAll(Model<MO, PK> model) {
        return db.selectAllQuery.createExecutableQuery(this, model).fetchAsync()
                .thenApply(result -> new NuSQLResult<>(model, result))
                .toCompletableFuture();
    }

    public <MO, PK> CompletableFuture<Stream<PK>> selectAllIds(Model<MO, PK> model) {
        return db.selectAllIdsQuery.createExecutableQuery(this, model).fetchAsync()
                .thenApply(result -> new NuSQLResult<>(model, result))
                .thenApply(result -> result.streamField(model.getPrimaryKey()))
                .toCompletableFuture();
    }



    // Overloads to clean up API for definitions above
    // Parameters here are extremely ugly, but are necessary in-practice
    public <MO, PK> CompletableFuture<Integer> insert(Class<? extends Model<MO, PK>> modelClazz, MO obj) {
        return insert(db.getModel(modelClazz), obj);
    }

    public <MO, PK> CompletableFuture<int[]> insert(Class<? extends Model<MO, PK>> modelClazz, Collection<MO> objs) {
        return insert(db.getModel(modelClazz), objs);
    }

    public <MO, PK> CompletableFuture<Integer> update(Class<? extends Model<MO, PK>> modelClazz, MO obj) {
        return update(db.getModel(modelClazz), obj);
    }

    public <MO, PK> CompletableFuture<int[]> update(Class<? extends Model<MO, PK>> modelClazz, Collection<MO> objs) {
        return update(db.getModel(modelClazz), objs);
    }

    public <MO, PK> CompletableFuture<Integer> upsert(Class<? extends Model<MO, PK>> modelClazz, MO obj) {
        return upsert(db.getModel(modelClazz), obj);
    }

    public <MO, PK> CompletableFuture<int[]> upsert(Class<? extends Model<MO, PK>> modelClazz, Collection<MO> objs) {
        return upsert(db.getModel(modelClazz), objs);
    }

    public <MO, PK> CompletableFuture<Integer> delete(Class<? extends Model<MO, PK>> modelClazz, MO obj) {
        return delete(db.getModel(modelClazz), obj);
    }

    public <MO, PK> CompletableFuture<int[]> delete(Class<? extends Model<MO, PK>> modelClazz, Collection<MO> objs) {
        return delete(db.getModel(modelClazz), objs);
    }

    public <MO, PK> CompletableFuture<Integer> deleteById(Class<? extends Model<MO, PK>> modelClazz, PK id) {
        return deleteById(db.getModel(modelClazz), id);
    }

    public <MO, PK> CompletableFuture<int[]> deleteByIds(Class<? extends Model<MO, PK>> modelClazz, Collection<PK> ids) {
        return deleteByIds(db.getModel(modelClazz), ids);
    }

    public <MO, PK> CompletableFuture<NuSQLResult<MO>> selectById(Class<? extends Model<MO, PK>> modelClazz, PK id) {
        return selectById(db.getModel(modelClazz), id);
    }

    public <MO, PK> CompletableFuture<NuSQLResult<MO>> selectAll(Class<? extends Model<MO, PK>> modelClazz) {
        return selectAll(db.getModel(modelClazz));
    }

    public <MO, PK> CompletableFuture<Stream<PK>> selectAllIds(Class<? extends Model<MO, PK>> modelClazz) {
        return selectAllIds(db.getModel(modelClazz));
    }*/
}
