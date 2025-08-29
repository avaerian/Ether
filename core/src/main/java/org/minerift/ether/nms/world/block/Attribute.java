package org.minerift.ether.nms.world.block;

import org.minerift.ether.debug.Debug;

import java.util.Collection;

// TODO: review this system in the future
// Acts as a placeholder to access BlockState Property's
public abstract class Attribute<T> {

    protected final int id;
    protected final String name;
    protected final Class<T> clazz;

    protected Attribute(int id, String name, Class<T> clazz) {
        this.id = id;
        this.name = name;
        this.clazz = clazz;
    }

    public int getId() {
        return id;
    }

    @Debug
    public String getName() {
        return name;
    }

    public abstract boolean isAcceptableValue(T val);
    public abstract Collection<T> getAcceptableValues();
}
