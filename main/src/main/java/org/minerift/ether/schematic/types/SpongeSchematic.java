package org.minerift.ether.schematic.types;

import org.minerift.ether.util.math.Vec3i;
import org.minerift.ether.world.BiomeArchetype;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.BlockEntityArchetype;
import org.minerift.ether.world.EntityArchetype;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpongeSchematic {



    public static class Builder {

        // sponge schematic version (differs from DataVersion; refers to Minecraft version)
        protected int version;
        protected short width, height, length;

        protected Vec3i offset;

        protected List<BlockArchetype> blocks;

        protected Map<Vec3i, BlockEntityArchetype> blockEntities;
        protected Set<EntityArchetype> entities;

        protected List<BiomeArchetype> biomes;



        // GETTERS

        public int getVersion() {
            return version;
        }

        public short getWidth() {
            return width;
        }

        public short getHeight() {
            return height;
        }

        public short getLength() {
            return length;
        }

        public Vec3i getOffset() {
            return offset;
        }

        public List<BlockArchetype> getBlocks() {
            return blocks;
        }

        public Map<Vec3i, BlockEntityArchetype> getBlockEntities() {
            return blockEntities;
        }

        public List<BiomeArchetype> getBiomes() {
            return biomes;
        }

        public Set<EntityArchetype> getEntities() {
            return entities;
        }


        // SETTERS

        public void setVersion(int version) {
            this.version = version;
        }

        public void setWidth(short width) {
            this.width = width;
        }

        public void setHeight(short height) {
            this.height = height;
        }

        public void setLength(short length) {
            this.length = length;
        }

        public void setOffset(Vec3i offset) {
            this.offset = offset;
        }

        public void setBlocks(List<BlockArchetype> blocks) {
            this.blocks = blocks;
        }

        public void setBlockEntities(Map<Vec3i, BlockEntityArchetype> blockEntities) {
            this.blockEntities = blockEntities;
        }

        public void setBiomes(List<BiomeArchetype> biomes) {
            this.biomes = biomes;
        }

        public void setEntities(Set<EntityArchetype> entities) {
            this.entities = entities;
        }
    }
}
