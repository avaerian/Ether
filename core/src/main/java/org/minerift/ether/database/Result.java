package org.minerift.ether.database;

import org.jetbrains.annotations.Nullable;
import org.minerift.ether.util.fn.IBuilder;

import java.util.Iterator;
import java.util.stream.Stream;

public abstract class Result<MO> implements Iterable<Record<MO>> {

    protected final @Nullable Model<MO, ?> model;

    public Result(Model<MO, ?> model) {
        this.model = model;
    }

    public Model<MO, ?> getModel() {
        return model;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public abstract int size();

    public abstract Record<MO> getRecord(int idx);

    public abstract Iterator<IBuilder<MO>> iterateBuilders();
    public abstract Stream<Record<MO>> stream();
    public Stream<MO> streamObjects() {
        return stream().map(Record::read);
    }

    public Stream<IBuilder<MO>> streamBuilders() {
        return stream().map(Record::readAsBuilder);
    }

    public <T> Stream<T> streamField(Field<MO, T, ?> field) {
        return stream().map(record -> record.get(field));
    }


}
