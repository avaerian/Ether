package org.minerift.ether.nms.world;

import org.bukkit.HeightMap;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec3;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.BiomeNotFoundException;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.NativeTypeConversions;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.BlockEntityArchetype;

import java.util.concurrent.CompletableFuture;

import static java.lang.String.format;

public interface Chunk<NBS, NC, NCS, NB> {

    static Chunk of(org.bukkit.Chunk bukkitChunk) {
        return Ether.inst().getNms().getConverter().asChunk(bukkitChunk);
    }

    static CompletableFuture<Chunk> of(World world, int cx, int cz) {
        return ChunkGetter.SYNC.getChunk(world, cx, cz);
    }

    int getX();
    int getZ();

    NCS[] getNativeSections();

    default Section[] getSections() {
        NCS[] nativeSections = getNativeSections();
        Section<NBS, NC, NCS, NB>[] sections = new Section[nativeSections.length];
        for(int i = 0; i < sections.length; i++) {
            sections[i] = getConverter().asChunkSection(nativeSections[i], i);
        }
        return sections;
    }

    NCS getNativeSection(int yIndex);

    default Section<NBS, NC, NCS, NB> getSection(int yIndex) {
        return getConverter().asChunkSection(getNativeSection(yIndex), yIndex);
    }



    NBS getNativeBlockState(int x, int y, int z);

    default NBS getNativeBlockState(Vec3 pos) {
        return getNativeBlockState(pos.getX(), pos.getY(), pos.getZ());
    }

    default BlockState<NBS> getBlockState(Vec3 vec) {
        return getBlockState(vec.getX(), vec.getY(), vec.getZ());
    }

    default BlockState<NBS> getBlockState(int x, int y, int z) {
        NBS nativeState = getNativeBlockState(x, y, z);
        return getConverter().asBlockState(nativeState);
    }

    NBS setNativeBlockState(int x, int y, int z, NBS nativeState);

    default BlockState<NBS> setBlockState(int x, int y, int z, BlockState<NBS> state) {
        NBS nativeOld = setNativeBlockState(x, y, z, state.asNative());
        return getConverter().asBlockState(nativeOld);
    }

    default BlockState<NBS> setBlockState(Vec3i blockPos, BlockState<NBS> state) {
        return setBlockState(blockPos.getX(), blockPos.getY(), blockPos.getZ(), state);
    }

    default BlockState<NBS> setBlockState(BlockArchetype block) {
        NBS nativeOld = setNativeBlockState(block.getX(), block.getY(), block.getZ(), (NBS) block.getState().asNative());
        return getConverter().asBlockState(nativeOld);
    }

    // Assumes that the state already has a block entity
    boolean setNativeBlockEntity(int x, int y, int z, NBS state, CompoundTag nbt);

    default boolean setBlockEntity(int x, int y, int z, BlockState<?> block, CompoundTag nbt) {
        return setNativeBlockEntity(x, y, z, (NBS) block.asNative(), nbt);
    }

    default boolean setBlockEntity(BlockEntityArchetype blockEntity) throws BlockStateNotFoundException {
        NBS nativeState = (NBS) blockEntity.getState().asNative();
        return setNativeBlockEntity(blockEntity.getX(), blockEntity.getY(), blockEntity.getZ(), nativeState, blockEntity.getAsNbt());
    }

    default boolean setBlockEntity(Vec3i pos, BlockState<?> block, CompoundTag nbt) {
        return setNativeBlockEntity(pos.getX(), pos.getY(), pos.getZ(), (NBS) block.asNative(), nbt);
    }

    NB getNativeBiome(int biomeX, int biomeY, int biomeZ);

    default Biome<?> getBiome(int biomeX, int biomeY, int biomeZ) {
        try {
            return getConverter().asBiome(getNativeBiome(biomeX, biomeY, biomeZ));
        } catch (BiomeNotFoundException ex) {
            throw new RuntimeException(format("Unexpected unidentifiable biome at %d, %d, %d", biomeX, biomeY, biomeZ), ex);
        }
    }

    void setBiome(int biomeX, int biomeY, int biomeZ, NB biome);

    default void setBiome(int biomeX, int biomeY, int biomeZ, Biome<?> biome) {
        setBiome(biomeX, biomeY, biomeZ, (NB) biome.asNative());
    }

    boolean updateNativeHeightmap(HeightMap heightmap, int x, int y, int z, NBS state);

    default boolean updateHeightmap(HeightMap heightmap, int x, int y, int z, BlockState<?> state) {
        return updateNativeHeightmap(heightmap, x, y, z, (NBS) state.asNative());
    }

    void setUnsaved(boolean needsSaving);
    boolean isUnsaved();

    void removeBlockEntity(int x, int y, int z); // TODO: must be synchronized??? -> review

    void sendChunkUpdatesPacket(Player plr, boolean modifyBlocks);
    default void sendChunkUpdatesPacket(Player plr) {
        sendChunkUpdatesPacket(plr, true);
    }

    void broadcastChunkUpdatesPacket();
    /*default void broadcastChunkUpdatesPacket() {
        broadcastChunkUpdatesPacket(true);
    }*/

    World getWorld();
    Object getNativeWorld();

    NC asNative();

    NativeTypeConversions<NBS, NC, NCS, NB, ?> getConverter();

    /*int getMinBuildHeight();
    int getMinSection();*/
}
