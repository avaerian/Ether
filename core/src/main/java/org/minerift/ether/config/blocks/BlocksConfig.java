package org.minerift.ether.config.blocks;

import lombok.Getter;
import lombok.Setter;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.source.FileSource;
import org.minerift.ether.island.CobblestoneGen;

public class BlocksConfig extends Config<BlocksConfig> {

    @Setter private boolean useCobblestoneGen;
    @Getter @Setter private CobblestoneGen mainCobblestoneGen;
    @Getter @Setter private CobblestoneGenHandler cobblestoneGenHandling;

    public BlocksConfig(ConfigRegistry reg, FileSource src) {
        super(reg, src);
        this.useCobblestoneGen  = true;
        this.mainCobblestoneGen = CobblestoneGen.defaults();
        this.cobblestoneGenHandling = CobblestoneGenHandler.PER_DIMENSION;
    }

    @Override
    protected void copyFrom(BlocksConfig o) {
        this.useCobblestoneGen  = o.useCobblestoneGen;
        this.mainCobblestoneGen = o.mainCobblestoneGen;
        this.cobblestoneGenHandling = o.cobblestoneGenHandling;
    }

    public boolean useCobblestoneGen() {
        return useCobblestoneGen;
    }

    @Override
    public ConfigType<BlocksConfig, FileSource> getType() {
        return ConfigType.BLOCKS;
    }
}
