package org.minerift.ether.world;

import org.bukkit.block.Biome;
import org.minerift.ether.math.Vec3i;

public class BiomeArchetype implements Archetype/*<Vec3i>*/ {

    private String biomeId;
    private Vec3i pos;

    public BiomeArchetype(Vec3i pos, String biomeId) {
        this.biomeId = biomeId;
        this.pos = pos.asMutable();
    }

    // NOTE: doesn't support custom biomes
    public Biome getBiome() {
        return BiomesList.getBiome(biomeId);
    }

    public String getBiomeId() {
        return biomeId;
    }

    //@Override
    public Vec3i getPos() {
        return pos;
    }

    public Vec3i.Mutable getPosMut() {
        return pos.asMutable();
    }

    //@Override
    public void setPos(Vec3i newPos) {
        this.pos = newPos;
    }
}
