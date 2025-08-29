package org.minerift.ether.schematic.data;

import org.minerift.ether.nms.world.Biome;
import org.minerift.ether.world.BiomeArchetype;

public class BiomeVolume extends Volume<Biome<?>> {
    public static BiomeVolume.Builder builder() {
        return new BiomeVolume.Builder();
    }

    public BiomeVolume(Array3DOrder order, byte[] data, BytePalette<Biome<?>> palette, int width, int height, int length) {
        super(order, data, palette, width, height, length);
    }

    @Deprecated
    protected BiomeVolume(int width, int height, int length) {
        super(width, height, length);
    }

    public static class Builder extends Volume.Builder<BiomeVolume, BiomeVolume.Builder, Biome<?>> {
        @Override
        public BiomeVolume build() {
            return new BiomeVolume(order, data, palette, width, height, length);
        }
    }
}
