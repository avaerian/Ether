package org.minerift.ether.schematic.sponge;

import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.data.BiomeVolume;
import org.minerift.ether.schematic.data.BlockVolume;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicType;
import org.minerift.ether.schematic.transform.Transforms;
import org.minerift.ether.util.Either;
import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.world.EntityArchetype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpongeSchematic implements Schematic {

    public static Builder builder() {
        return new SpongeSchematic.Builder();
    }

    private final int width, height, length;
    private final Vec3i offset;
    private final BlockVolume blocks;
    private final BiomeVolume biomes;
    private final List<EntityArchetype> entities;

    public SpongeSchematic(int width, int height, int length,
                           Vec3i offset,
                           BlockVolume blocks, BiomeVolume biomes,
                           List<EntityArchetype> entities) {
        this.width = width;
        this.height = height;
        this.length = length;
        this.offset = offset;
        this.blocks = blocks;
        this.biomes = biomes;
        this.entities = entities;
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
    public Vec3i getDimensions() {
        return new Vec3i(width, height, length);
    }

    @Override
    public Vec3i getOffset() {
        return offset;
    }

    @Override
    public SpongeSchematic transform(Transforms transforms) {
        // TODO: get blockvolume pasting working, test in-game, fix rotational issues
        // TODO: commit changes, impl Flip transform
        return null;
    }

    public BlockVolume getBlocks() {
        return blocks;
    }

    public BiomeVolume getBiomes() {
        return biomes;
    }

    public List<EntityArchetype> getEntities() {
        return entities;
    }

    public static class Builder implements IBuilder<SpongeSchematic> {

        private SpongeVersion version;
        private Vec3i dim;
        private Vec3i offset;
        //private Either<BlockVolume, BlockVolume.Builder> blocks;
        private BlockVolume.Builder blocks;
        private Either<BiomeVolume, BiomeVolume.Builder> biomes;
        private List<EntityArchetype> entities;

        protected Builder() {
            this.version = SpongeVersion.UNKNOWN; // TODO: change this to better default???
            this.dim = Vec3i.ZERO;
            this.offset = Vec3i.ZERO;
            this.blocks = null;
            this.biomes = null;
            this.entities = Collections.emptyList();
        }

        public Builder setVersion(SpongeVersion version) {
            this.version = version;
            return this;
        }

        public Builder setDimensions(int width, int height, int length) {
            this.dim = new Vec3i(width, height, length);
            return this;
        }

        public Builder setDimensions(Vec3i dim) {
            return setDimensions(dim.getX(), dim.getY(), dim.getZ());
        }

        public Builder setOffset(Vec3i offset) {
            this.offset = offset;
            return this;
        }

        public Builder setBlocks(BlockVolume.Builder blocks) {
            this.blocks = blocks;
            return this;
        }

        public Builder setBiomes(BiomeVolume biomes) {
            this.biomes = Either.left(biomes);
            return this;
        }

        public Builder setBiomes(BiomeVolume.Builder biomes) {
            this.biomes = Either.right(biomes);
            return this;
        }

        public Builder setEntities(List<EntityArchetype> entities) {
            this.entities = entities;
            return this;
        }

        public Builder addEntity(EntityArchetype entity) {
            if (Maths.inRangeI(Vec3i.ZERO, dim, entity.getPos())) // TODO

                if (entities == Collections.EMPTY_LIST) {
                    this.entities = new ArrayList<>();
                }
            return this;
        }

        public SpongeVersion getVersion() {
            return version;
        }

        public Vec3i getDimensions() {
            return dim;
        }

        public int getWidth() {
            return dim.getX();
        }

        public int getHeight() {
            return dim.getY();
        }

        public int getLength() {
            return dim.getZ();
        }

        public Vec3i getOffset() {
            return offset;
        }

        public BlockVolume.Builder getBlocks() {
            return blocks;
        }

        public Either<BiomeVolume, BiomeVolume.Builder> getBiomes() {
            return biomes;
        }

        public List<EntityArchetype> getEntities() {
            return entities;
        }

        @Override
        public SpongeSchematic build() {
            BlockVolume blocks = this.blocks.build();
            BiomeVolume biomes = this.biomes.isLeft() ? this.biomes.getLeft() : this.biomes.getRight().build();
            return new SpongeSchematic(dim.getX(), dim.getY(), dim.getZ(), offset, blocks, biomes, entities);
        }
    }
}
