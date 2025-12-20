package org.minerift.ether.config.islandspecs;

import org.jetbrains.annotations.NotNull;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.source.DirectorySource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class IslandSpecsConfig extends Config<IslandSpecsConfig> implements Iterable<IslandSpec> {

    protected List<IslandSpec> islandSpecs;

    public IslandSpecsConfig(ConfigRegistry reg, DirectorySource src) {
        super(reg, src);
        this.islandSpecs = Collections.emptyList();
    }

    public boolean add(IslandSpec spec) {
        if(islandSpecs == Collections.EMPTY_LIST) {
            islandSpecs = new ArrayList<>();
        }
        islandSpecs.add(spec);
        return true;
    }

    @Override
    protected void copyFrom(IslandSpecsConfig other) {
        this.islandSpecs = other.islandSpecs;
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
