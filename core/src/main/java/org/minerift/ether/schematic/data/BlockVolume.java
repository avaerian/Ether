package org.minerift.ether.schematic.data;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.block.Attribute;
import org.minerift.ether.nms.world.block.Attributes;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.schematic.transform.Direction;
import org.minerift.ether.schematic.transform.Transform;
import org.minerift.ether.schematic.transform.Transforms;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.BlockEntityArchetype;

import java.util.BitSet;
import java.util.Collection;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.minerift.ether.schematic.transform.Direction.*;

public class BlockVolume extends Volume<BlockState<?>> {

    public static final int NO_FLAGS;
    public static final int ALL_FLAGS;

    public static final int COPY_BV;
    public static final int COPY_PALETTE;
    public static final int ROTATE_BLK_DIRS;

    static {
        int flag = 0;

        NO_FLAGS = 0;
        COPY_BV = 1 << flag++;
        COPY_PALETTE = 1 << flag++;
        ROTATE_BLK_DIRS = 1 << flag++;

        ALL_FLAGS = (1 << flag) - 1;
    }

    public static BlockVolume.Builder builder() {
        return new BlockVolume.Builder();
    }

    public final Int2ObjectMap<BlockEntityArchetype> blockEntities;
    private final BitSet blockEntityTest;


    // TODO: improve block entity handling by checking if BlockState
    //  has a block entity and handling, propagating fn call??

    public BlockVolume(Array3DOrder order, byte[] data, BytePalette<BlockState<?>> palette,
                       Vec3i dim, Int2ObjectMap<BlockEntityArchetype> blockEntities) {
        this(order, data, palette, dim.getX(), dim.getY(), dim.getZ(), blockEntities);
    }

    public BlockVolume(Array3DOrder order, byte[] data, BytePalette<BlockState<?>> palette,
                       int width, int height, int length,
                       Int2ObjectMap<BlockEntityArchetype> blockEntities) {
        super(order, data, palette, width, height, length);
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

    // TODO: move up to Volume.class (or Transformable ??)
    // TODO: implement method for Schematic.class
    public BlockVolume transform(Transform t) {
        return transform(Transforms.of(t), 0);
    }

    public BlockVolume transform(Transform t, int flags) {
        return transform(Transforms.of(t), flags);
    }

    public BlockVolume transform(Transforms ts) {
        return transform(ts, 0);
    }

    // Return [immutable copy of] transformed BlockVolume
    public BlockVolume transform(Transforms ts, int flags) {
        byte[] buf;
        if((flags & COPY_BV) != 0) {
            buf = new byte[data.length];
            System.arraycopy(data, 0, buf, 0, data.length);
        } else {
            buf = data;
        }

        // rotate palette
        BytePalette<BlockState<?>> palette;
        if((flags & COPY_PALETTE) != 0) {
            palette = this.palette.copy();
        } else {
            palette = this.palette;
        }

        if((flags & ROTATE_BLK_DIRS) != 0) {
            for(BytePalette.Entry<BlockState<?>> entry : palette) {
                BlockState<?> state = entry.getValue();
                Attribute<Direction> dirAttr;
                Direction dir;

                // NOTE: multiple mutable vec copies are created; should fix
                if (state.hasAttribute(Attributes.AXIS)) {
                    dir = switch (state.getAttribute(Attributes.AXIS)) {
                        case X -> NORTH;
                        case Y -> UP;
                        case Z -> EAST;
                    };
                    Transform.Result<Vec3i> res = ts.apply(ROT_MATRIX_SIZE, dir.getNormal().asMutableCopy().add(1, 1, 1));
                    Direction rotated = Direction.fromVector(res.out.asMutableCopy().subtract(1, 1, 1));

                    palette.add(entry.getKey(), state.trySetAttribute(Attributes.AXIS, rotated.getAxis()), true);
                } else if ((dir = state.tryGetAttribute(dirAttr = Attributes.FACING)) != null
                        || ((dir = state.tryGetAttribute(dirAttr = Attributes.HORIZONTAL_FACING)) != null)) {

                    Transform.Result<Vec3i> res = ts.apply(ROT_MATRIX_SIZE, dir.getNormal().asMutableCopy().add(1, 1, 1));
                    Direction rotated = Direction.fromVector(res.out.asMutableCopy().subtract(1, 1, 1));

                    palette.add(entry.getKey(), state.trySetAttribute(dirAttr, rotated), true);
                }
            }
        }

        Transform.Result<byte[]> res = ts.apply(order, width, height, length, buf);

        // transform BlockEntity positions
        Int2ObjectMap<BlockEntityArchetype> newBlockEntities = new Int2ObjectOpenHashMap<>(blockEntities.size());
        for(Int2ObjectMap.Entry<BlockEntityArchetype> be : blockEntities.int2ObjectEntrySet()) {
            Vec3i oldPos = order.unflatten(width, length, be.getIntKey());
            Transform.Result<Vec3i> newPos = ts.apply(width, height, length, oldPos);
            int newFlat = order.flatten(res.dim.getX(), res.dim.getZ(), newPos.out);
            BlockEntityArchetype newBe = new BlockEntityArchetype(be.getValue().getState(), newPos.out, be.getValue().getNbtData());
            newBlockEntities.put(newFlat, newBe);
        }

        return new BlockVolume(order, res.out, palette, res.dim, newBlockEntities);
    }

    public static class Builder extends Volume.Builder<BlockVolume, BlockVolume.Builder, BlockState<?>> {

        public static final Supplier<Int2ObjectMap<BlockEntityArchetype>> NEW_BLOCK_ENTITY_MAP = Int2ObjectOpenHashMap::new;

        public Int2ObjectMap<BlockEntityArchetype> blockEntities;

        protected Builder() {
            super();
            this.blockEntities = Int2ObjectMaps.emptyMap();
        }

        public Builder addBlockEntity(BlockEntityArchetype bEntity) {
            Preconditions.checkState(width > 0, "Width must be greater than 0");
            Preconditions.checkState(height > 0, "Height must be greater than 0");
            Preconditions.checkState(length > 0, "Length must be greater than 0");

            if(blockEntities == Int2ObjectMaps.EMPTY_MAP) {
                this.blockEntities = NEW_BLOCK_ENTITY_MAP.get();
            }

            Vec3i pos = bEntity.getPos().copyAsImmutable();

            // ensure block entity pos is within volume bounds
            if(!Maths.inRangeI(Vec3i.ZERO, new Vec3i(width, height, length), pos)) {
                throw new IllegalArgumentException(
                        format("Block entity at %s is outside of volume bounds (%d, %d, %d)",
                                bEntity.getPos(), width, height, length));
            }

            int idx = order.flatten(width, length, pos.getX(), pos.getY(), pos.getZ());
            blockEntities.put(idx, bEntity);
            return this;
        }

        public Builder setBlockEntities(Int2ObjectMap<BlockEntityArchetype> blockEntities) {
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
