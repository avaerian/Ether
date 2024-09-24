package org.minerift.ether.world;

import org.bukkit.block.Biome;
import org.minerift.ether.math.Vec3i;

public class BiomeArchetype {

    private final Biome biome; // TODO: switch to String/NamespacedKey for custom biomes?
    private final Vec3i.Mutable pos;

    public BiomeArchetype(Biome biome, Vec3i pos) {
        this.biome = biome;
        this.pos = pos.asMutable();
    }

    public Biome getBiome() {
        return biome;
    }

    public Vec3i.Mutable getPos() {
        return pos;
    }
}
