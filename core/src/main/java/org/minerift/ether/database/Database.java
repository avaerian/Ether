package org.minerift.ether.database;

import org.minerift.ether.database.nusql.NuSQLAccess;
import org.minerift.ether.database.nusql.NuSQLDatabase;
import org.minerift.ether.util.Utils;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public abstract class Database implements AutoCloseable {

    protected final String dbName;
    protected Map<Class<? extends Model>, Model<?, ?>> models;
    protected Map<String, Class<? extends Model>> tableNamesToModels;

    public Database(String dbName) {
        this.dbName = dbName;
        this.models = Collections.emptyMap();
        this.tableNamesToModels = Collections.emptyMap();
    }

    public enum Type {
        SQL,
        BIN,

        ;

        public static Type valueOfSilent(String str) {
            return Utils.valueOfSilent(Type.class, str.toUpperCase());
        }
    }

    public interface DbAccessFunction {
        void accept(DatabaseAccess access) throws DatabaseException;
    }

    public String getName() {
        return dbName;
    }

    public CompletableFuture<DatabaseException> access(DbAccessFunction proc) {
        return access(false, true, proc);
    }

    public CompletableFuture<DatabaseException> accessSync(DbAccessFunction proc) {
        return access(false, false, proc);
    }

    public abstract CompletableFuture<DatabaseException> access(boolean autocommit, boolean async, DbAccessFunction proc);

    public <M extends Model> M getModel(Class<M> modelClazz) {
        return (M) models.get(modelClazz);
    }

    public Model<?, ?> getModel(String tableName) {
        return getModel(getModelClass(tableName));
    }

    public Collection<Model<?, ?>> getModels() {
        return models.values();
    }

    public Class<? extends Model> getModelClass(String tableName) {
        return tableNamesToModels.get(tableName);
    }

    public Map<Class<? extends Model>, Model<?, ?>> getModelsMap() {
        return models;
    }
}
