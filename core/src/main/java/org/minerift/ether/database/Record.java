package org.minerift.ether.database;

import org.minerift.ether.util.fn.IBuilder;

import java.util.function.Function;

public abstract class Record<MO> {

    protected final Model<MO, ?> model;

    public Record(Model<MO, ?> model) {
        this.model = model;
    }

    public Model<MO, ?> getModel() {
        return model;
    }

    public abstract <T> T get(Field<?, T, ?> field);
    public abstract <C, T> C get(Field.FieldWithAdapter<?, C, T, ?> complexField);
    public abstract Object get(String column);
    public abstract Object get(int idx);

    public <T> T get(String column, Class<? extends T> cast) {
        return cast.cast(get(column));
    }
    public <M> M get(String column, Function<Object, ? extends M> mapper) {
        return mapper.apply(get(column));
    }

    public <T> T get(int idx, Class<? extends T> cast) {
        return cast.cast(get(idx));
    }
    public <M> M get(int idx, Function<Object, ? extends M> mapper) {
        return mapper.apply(get(idx));
    }


    public MO read() {
        return model.readRecord(this);
    }

    public IBuilder<MO> readAsBuilder() {
        return model.readAsBuilder(this);
    }

}
