package org.minerift.ether.config.islandspecs;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IslandSpecsConfig extends Config<IslandSpecsConfig> implements Iterable<IslandSpec> {

    public static final IslandSpecsCodec CODEC = IslandSpecsCodec.CODEC;

    protected List<IslandSpec> islandSpecs; // FIXME: make private and add methods that modify setChanged()

    public IslandSpecsConfig() {
        this.islandSpecs = new ArrayList<>();
    }

    @Override
    protected void copyFrom(IslandSpecsConfig other) {
        if(!other.equals(this)) {
            this.islandSpecs = other.islandSpecs;
            setChanged(true);
        }
    }

    @Override
    public ConfigType<IslandSpecsConfig> getType() {
        return ConfigType.ISLAND_SPECS_LIST;
    }

    @NotNull
    @Override
    public Iterator<IslandSpec> iterator() {
        return islandSpecs.iterator();
    }
}
