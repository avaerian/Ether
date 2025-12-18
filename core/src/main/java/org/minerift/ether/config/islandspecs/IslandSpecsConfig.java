package org.minerift.ether.config.islandspecs;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.source.DirectorySource;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IslandSpecsConfig extends Config<IslandSpecsConfig> implements Iterable<IslandSpec> {

    public static final IslandSpecsCodec CODEC = IslandSpecsCodec.INST;

    protected List<IslandSpec> islandSpecs; // FIXME: make private and add methods that modify setChanged()

    public IslandSpecsConfig() {
        this.islandSpecs = new ArrayList<>();
    }

    // TODO: review
    public boolean add(IslandSpec spec) {
        islandSpecs.add(spec);
        return true;
    }

    @Override
    protected void copyFrom(IslandSpecsConfig other) {
        if(!other.equals(this)) {
            this.islandSpecs = other.islandSpecs;
        }
    }

    @Override
    public ConfigType<IslandSpecsConfig, DirectorySource> getType() {
        return ConfigType.ISLAND_SPECS_LIST;
    }

    @NotNull
    @Override
    public Iterator<IslandSpec> iterator() {
        return islandSpecs.iterator();
    }
}
