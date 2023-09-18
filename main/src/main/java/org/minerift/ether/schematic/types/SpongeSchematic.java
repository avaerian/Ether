package org.minerift.ether.schematic.types;

import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.pasters.SpongeSchematicPaster;
import org.minerift.ether.util.math.Vec3i;
import org.minerift.ether.world.BiomeArchetype;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.BlockEntityArchetype;
import org.minerift.ether.world.EntityArchetype;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SpongeSchematic implements Schematic {

    private int width, height, length;
    private Vec3i offset;
    private List<BlockArchetype> blocks;
    private List<BiomeArchetype> biomes;
    private Set<EntityArchetype> entities;

    public static SpongeSchematic.Builder builder() {
        return new SpongeSchematic.Builder();
    }

    private SpongeSchematic(SpongeSchematic.Builder builder) {
        this.width = builder.getWidth();
        this.height = builder.getHeight();
        this.length = builder.getLength();
        this.offset = builder.getOffset();
        this.blocks = builder.getBlocks();
        this.biomes = builder.getBiomes();
        this.entities = builder.getEntities();
    }

    @Override
    public SchematicType getType() {
        return SchematicType.SPONGE;
    }

    @Override
    public void paste(Vec3i pos, String worldName, SchematicPasteOptions options) {
        getType().getPaster(SpongeSchematicPaster.class).paste(this, pos, worldName, options);
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Vec3i getOffset() {
        return offset;
    }

    public List<BlockArchetype> getBlocks() {
        return blocks;
    }

    public List<BiomeArchetype> getBiomes() {
        return biomes;
    }

    public Set<EntityArchetype> getEntities() {
        return entities;
    }

    public static class Builder {

        // sponge schematic version (differs from DataVersion; refers to Minecraft version)
        private int version;
        private short width, height, length;
        private Vec3i offset;
        private List<BlockArchetype> blocks;
        private Map<Vec3i, BlockEntityArchetype> blockEntities;
        private Set<EntityArchetype> entities;
        private List<BiomeArchetype> biomes;

        private Builder() {
            this.version = 0;
            this.width = 0;
            this.height = 0;
            this.length = 0;
            this.offset = Vec3i.ZERO;
            this.blocks = Collections.emptyList();
            this.biomes = Collections.emptyList();
            this.blockEntities = Collections.emptyMap();
            this.entities = Collections.emptySet();
        }

        public SpongeSchematic build() {
            return new SpongeSchematic(this);
        }

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
