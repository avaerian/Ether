package org.minerift.ether.database;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;

public class DatabaseCreationContext {

    protected Map<Class<? extends Model>, Model<?, ?>> models;
    // TODO: for foreign fields from different tables referencing each other, could queue up
    //   here and experiment; needs work and planning for later

    public DatabaseCreationContext(Map<Class<? extends Model>, Model<?, ?>> models) {
        this.models = models;
    }

    public void registerModels(Function<DatabaseCreationContext, Model<?, ?>>... models) {
        for(var modelCreator : models) {
            registerModel(modelCreator.apply(this));
        }
    }

    protected void registerModel(Model<?, ?> model) {
        if(models.containsKey(model.getClass())) {
            throw new DatabaseException("Model " + model.getClass().getSimpleName() + " already registered");
        }
        models.put(model.getClass(), model);
    }

    public <M extends Model> M getModel(Class<M> modelClazz) {
        if(!models.containsKey(modelClazz)) {
            throw new DatabaseException("Model " + modelClazz.getSimpleName() + " not registered when attempting to access");
        }
        return (M) models.get(modelClazz);
    }

    public Collection<Model<?, ?>> getModels() {
        return models.values();
    }

    public Map<Class<? extends Model>, Model<?, ?>> getModelsMap() {
        return models;
    }
}
