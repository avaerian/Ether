package org.minerift.ether.database;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public abstract class Database {

    protected final String dbName;
    protected Map<Class<? extends Model>, Model<?, ?>> models;

    public Database(String dbName) {
        this.dbName = dbName;
        this.models = Collections.emptyMap();
    }

    public String getName() {
        return dbName;
    }

    public <M extends Model> M getModel(Class<M> modelClazz) {
        return (M) models.get(modelClazz);
    }

    public Collection<Model<?, ?>> getModels() {
        return models.values();
    }

    public Map<Class<? extends Model>, Model<?, ?>> getModelsMap() {
        return models;
    }

}
