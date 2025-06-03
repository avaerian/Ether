package org.minerift.ether.schematic.data;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.BlockState;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.BlockEntityArchetype;

import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import static java.lang.String.format;

public class BlockVolume extends Volume<BlockState<?>, BlockArchetype> {

    public static BlockVolume.Builder builder() {
        return new BlockVolume.Builder();
    }

    private final Map<Integer, BlockEntityArchetype> blockEntities;
    private final BitSet blockEntityTest;

    public BlockVolume(Array3DOrder order, byte[] data, BytePalette<BlockState<?>> palette,
                       int width, int height, int length,
                       Map<Integer, BlockEntityArchetype> blockEntities) {
        super(order, data, palette, width, height, length, BlockArchetype::new);
        this.blockEntities = blockEntities;

        // From block entities, create bit test
        BitSet blockEntityTest = new BitSet(data.length);
        blockEntities.forEach((i, ignore) -> blockEntityTest.set(i));
        this.blockEntityTest = blockEntityTest;
    }

    public boolean isBlockEntity(int idx) {
        return blockEntityTest.get(idx);
    }

    public boolean isBlockEntity(int x, int y, int z) {
        int idx = order.flatten(width, length, x, y, z);
        return isBlockEntity(idx);
    }

    public boolean isBlockEntity(Vec3i pos) {
        return isBlockEntity(pos.getX(), pos.getY(), pos.getZ());
    }

    // NOTE: can return null if block entity doesn't exist at index
    public BlockEntityArchetype getBlockEntity(int idx) {
        return blockEntities.get(idx);
    }

    public BlockEntityArchetype getBlockEntity(int x, int y, int z) {
        int idx = order.flatten(width, length, x, y, z);
        return getBlockEntity(idx);
    }

    public BlockEntityArchetype getBlockEntity(Vec3i pos) {
        return getBlockEntity(pos.getX(), pos.getY(), pos.getZ());
    }

    public Collection<BlockEntityArchetype> getBlockEntities() {
        return blockEntities.values();
    }

    public static class Builder extends Volume.Builder<BlockVolume, BlockVolume.Builder, BlockState<?>, BlockArchetype> {

        public static final Supplier<Map<Integer, BlockEntityArchetype>> NEW_BLOCK_ENTITY_MAP = Int2ObjectOpenHashMap::new;

        protected Map<Integer, BlockEntityArchetype> blockEntities;

        protected Builder() {
            super();
            this.blockEntities = Collections.emptyMap();
        }

        public Builder addBlockEntity(BlockEntityArchetype bEntity) {
            Preconditions.checkState(width > 0, "Width must be greater than 0");
            Preconditions.checkState(height > 0, "Height must be greater than 0");
            Preconditions.checkState(length > 0, "Length must be greater than 0");

            if(blockEntities == Collections.EMPTY_MAP) {
                this.blockEntities = NEW_BLOCK_ENTITY_MAP.get();
            }

            Vec3i pos = bEntity.getPos().copyAsImmutable();

            // ensure block entity pos is within volume bounds
            // FIXME: switch to different volume bounds check
            if(pos.isGreaterThan(new Vec3i(width, height, length), true)) {
                throw new IllegalArgumentException(
                        format("Block entity at %s is outside of volume bounds (%d, %d, %d)",
                                bEntity.getPos(), width, height, length));
            }

            int idx = order.flatten(width, length, pos.getX(), pos.getY(), pos.getZ());
            blockEntities.put(idx, bEntity);
            return this;
        }

        public Builder setBlockEntities(Map<Integer, BlockEntityArchetype> blockEntities) {
            this.blockEntities = blockEntities;
            return this;
        }

        @Override
        public BlockVolume build() {
            Preconditions.checkState(data.length == width * height * length,
                    "Data length and volume dimensions are mismatched");

            return new BlockVolume(order, data, palette, width, height, length, blockEntities);
        }
    }
}
