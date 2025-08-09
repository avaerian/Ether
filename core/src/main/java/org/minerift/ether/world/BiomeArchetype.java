package org.minerift.ether.world;

import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.BiomeNotFoundException;
import org.minerift.ether.nms.world.Biome;

public class BiomeArchetype implements Archetype/*<Vec3i>*/ {

    private Biome<?> biome;
    private Vec3i pos;

    public BiomeArchetype(String biomeId, Vec3i pos) throws BiomeNotFoundException {
        this.biome = Biome.of(biomeId);
        this.pos = pos.asMutable();
    }

    public BiomeArchetype(Biome<?> biome, Vec3i pos) {
        this.biome = biome;
        this.pos = pos;
    }

    public Biome<?> getBiome() {
        return biome;
    }

    public String getBiomeId() {
        return biome.getResourceKey();
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
