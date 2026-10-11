package org.minerift.ether.nms.world;

import org.bukkit.entity.Player;
import org.minerift.ether.math.Vec3;
import org.minerift.ether.nms.BiomeNotFoundException;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.NativeTypeConversions;
import org.minerift.ether.nms.world.block.BlockState;

import static java.lang.String.format;

public interface Section<NBS, NC, NCS, NB> {

    void acquire();

    void release();

    NBS getNativeBlockState(int x, int y, int z);

    default BlockState<?> getBlockState(int x, int y, int z) {
        return getConverter().asBlockState(getNativeBlockState(x, y, z));
    }

    default BlockState<?> getBlockState(Vec3 pos) {
        return getBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    NBS setNativeBlockState(int x, int y, int z, NBS state);

    default NBS setBlockState(int x, int y, int z, String id) throws BlockStateNotFoundException {
        NBS state = getConverter().asNativeBlockState(id);
        NBS old = setNativeBlockState(x, y, z, state);
        return old;
    }

    default BlockState<?> setBlockState(int x, int y, int z, BlockState<?> state) {
        NBS oldNative = setNativeBlockState(x, y, z, (NBS)state.asNative());
        BlockState<NBS> old = getConverter().asBlockState(oldNative);
        return old;
    }

    NB getNativeBiome(int biomeX, int biomeY, int biomeZ);

    default Biome<?> getBiome(int biomeX, int biomeY, int biomeZ) {
        try {
            return getConverter().asBiome(getNativeBiome(biomeX, biomeY, biomeZ));
        } catch (BiomeNotFoundException ex) {
            throw new RuntimeException(format("Unexpected unidentifiable biome at %d, %d, %d", biomeX, biomeY, biomeZ), ex);
        }
    }

    void setNativeBiome(int biomeX, int biomeY, int biomeZ, NB biome);

    /**
     * Set the biome for a location in this chunk section.
     *
     * <p>
     * <b>NOTE:</b> the input position for biomes is a <u>QuartPos</u>, a.k.a. a quarter
     * of a chunk. A biome can be set every 4 blocks.
     *
     * @param biomeX quart position relative to this section
     * @param biomeY quart position relative to this section
     * @param biomeZ quart position relative to this section
     * @param biome the NMS biome
     */
    default void setBiome(int biomeX, int biomeY, int biomeZ, Biome<?> biome) {
        setNativeBiome(biomeX, biomeY, biomeZ, (NB) biome.asNative());
    }

    void updateSectionChanges(int sectionIndex, ChunkSectionChanges changes);

    default void sendSectionUpdatesPacket(Player plr, ChunkSectionChanges changes) {
        sendSectionUpdatesPacket(plr, changes, true);
    }

    void sendSectionUpdatesPacket(Player plr, ChunkSectionChanges changes, boolean modifyBlocks);

    void broadcastSectionUpdatesPacket(ChunkSectionChanges changes, boolean modifyBlocks);
    default void broadcastSectionUpdatesPacket(ChunkSectionChanges changes) {
        broadcastSectionUpdatesPacket(changes, true);
    }

    NCS asNative();

    NativeTypeConversions<NBS, NC, NCS, NB, ?> getConverter();


    // Util functions

    // Rounding
    static int sectionRelative(int i) {
        return i & 15;
    }

    // For packets
    static short sectionRelativePos(int x, int y, int z) {
        return (short) ((x & 15) << 8 | (z & 15) << 4 | y & 15);
    }

    // Internal array index
    static int getSectionIdx(int blockY, int minHeight) {
        return (blockY >> 4) - (minHeight >> 4);
    }

    // World position from array index
    static int sectionRealFromIdx(int sectionIdx, int minHeight) {
        return sectionIdx + (minHeight >> 4);
    }

    // World position
    static int getSectionReal(int blockY) {
        return (blockY >> 4);
    }

    // TODO: review these based on NMS version
    boolean hasOnlyAir();
    boolean isRandomlyTicking();
    boolean isRandomlyTickingBlocks();
    boolean isRandomlyTickingFluids();

    int getSpecialCollidingBlocks();
    int bottomBlockY();

}
