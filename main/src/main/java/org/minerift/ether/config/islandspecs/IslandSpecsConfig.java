package org.minerift.ether.config.islandspecs;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigType;

import java.util.List;

public class IslandSpecsConfig extends Config<IslandSpecsConfig> {

    private List<IslandSpec> islandSpecs;

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
}
