package org.minerift.ether.config;

import org.minerift.ether.config.source.Source;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

// NOTE: writing a good equality check is extremely useful for checking
//  for modifications between saving configs
public abstract class Config<T extends Config<T>> {

    public interface CreateConfigFn<T extends Config<T>, S extends Source> {
        T create(ConfigRegistry reg, S src);
    }

    public interface SourceSupplier<S extends Source> {
        S create() throws Exception; // user can change to throw whatever exception they want
    }

    protected final ReadWriteLock rwLock;
    protected final ConfigRegistry reg;
    protected final Source src;

    public Config(ConfigRegistry reg, Source src) {
        this.rwLock = new ReentrantReadWriteLock();
        this.reg = reg;
        this.src = src;
    }

    protected Lock readLock() {
        return rwLock.readLock();
    }

    protected Lock writeLock() {
        return rwLock.writeLock();
    }

    public void save() throws ConfigWriteException {
        ((ConfigType)getType()).codec().write(this, src);
    }

    // returns whether cfg reloaded
    public boolean reload() throws ConfigReadException {
        final ConfigCodec<T, Source> codec = (ConfigCodec<T, Source>)getType().codec();

        T reload = ((ConfigType<T, Source>)getType()).getDefaultConfig(reg, src);
        try {
            codec.read(reload, src);
        } catch (ConfigNotFoundException ex) {
            //reload = (T) ((ConfigType)getType()).getDefaultConfig(reg, src);
            // failed to reload; don't change current settings
            return false;
        }
        copyFrom(reload);
        return true;
    }

    // Copies data from a similar config over to this config
    // This is used for reloading; a new config object will be created
    // with the new settings loaded, so we want to copy that data over
    // to the primary config object
    protected abstract void copyFrom(T other);

    public abstract ConfigType<T, ?> getType();

    public String getTypeName() {
        return getType().getName();
    }
}
