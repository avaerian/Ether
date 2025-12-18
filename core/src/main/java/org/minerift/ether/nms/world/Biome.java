package org.minerift.ether.nms.world;

import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.BiomeNotFoundException;
import org.minerift.ether.nms.NativeTypeConversions;

// TODO: NBH, NB ????
public interface Biome<NB> {

    static Biome<?> of(String id) throws BiomeNotFoundException {
        return Ether.inst().getNms().getConverter().asBiome(id);
    }

    static Biome<?> of(String id, @Nullable String fallback) {
        return Ether.inst().getNms().getConverter().asBiome(id, fallback);
    }


    boolean hasPrecipitation();
    int getFogColor();
    int getFoliageColor();
    int getWaterColor();

    String getResourceKey();
    NamespacedKey getNamespacedKey();


    NativeTypeConversions<?,?,?,NB,?> getConverter();

    NB asNative();

}
