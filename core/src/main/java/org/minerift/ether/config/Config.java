package org.minerift.ether.config;

import org.minerift.ether.config.source.Source;

// NOTE: writing a good equality check is extremely useful for checking
//  for modifications between saving configs
public abstract class Config<T extends Config<T>> {

    // TODO: for mutability (because that seems to be the most appropriate approach), explore
    //  locks and creating nice ways of updating an abundance of properties

    //protected final ConfigRegistry registry;
    /*protected final Source src;

    public Config(Source src) {
        this.src = src;
    }*/



    // TODO: move to config registry
    public void save() throws ConfigWriteException {
        ((ConfigCodec)getType().codec()).write(this, src);
    }

    @Deprecated //FIXME
    public void saveIfChanged() {
        if(hasChanged()) {
            save();
        }
    }

    // Loads from file again
    // Returns whether the file reloaded successfully
    // TODO: locking; move to config registry
    public boolean reload() throws ConfigReadException {
        final ConfigCodec<T, Source> codec = (ConfigCodec<T, Source>) getType().codec();
        T reload;
        try {
            reload = codec.read(src);
        } catch (ConfigNotFoundException ex) {
            reload = getType().getDefaultConfig();
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

    public String getName() {
        return getType().getName();
    }
}
