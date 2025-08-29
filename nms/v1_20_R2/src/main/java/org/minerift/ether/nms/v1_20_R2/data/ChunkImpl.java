package org.minerift.ether.nms.v1_20_R2.data;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import org.bukkit.HeightMap;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.minerift.ether.nms.v1_20_R2.NativeTypeConversionsImpl;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class ChunkImpl implements Chunk<BlockState, LevelChunk, LevelChunkSection, Holder<Biome>> {

    // TODO: cache ??
    private static final Map<ChunkAccess, ChunkImpl> CACHE = new WeakHashMap<>();

    private final LevelChunk chunk;

    public static ChunkImpl of(LevelChunk nChunk) {
        return CACHE.computeIfAbsent(nChunk, ChunkImpl::new);
    }

    public static ChunkImpl of(ChunkAccess nChunk) {
        return CACHE.computeIfAbsent(nChunk, ChunkImpl::new);
    }

    private ChunkImpl(LevelChunk chunk) {
        this.chunk = chunk;
    }

    private ChunkImpl(ChunkAccess chunk) {
        if(chunk instanceof ProtoChunk) {
            throw new UnsupportedOperationException("ChunkAccess chunk is not a LevelChunk: " + chunk.getPos());
        }
        this.chunk = (LevelChunk) chunk;
    }

    // Assumes that the state already has a block entity
    @Override
    public boolean setNativeBlockEntity(int x, int y, int z, BlockState state, CompoundTag nbt) {
        BlockPos blockPos = new BlockPos(x, y, z);
        BlockEntity blockEntity = ((EntityBlock) state.getBlock()).newBlockEntity(blockPos, state);
        if(blockEntity != null) {
            chunk.setBlockEntity(blockEntity);

            // Load NBT data
            net.minecraft.nbt.CompoundTag nativeNbt = (net.minecraft.nbt.CompoundTag) getConverter().asNativeTag(nbt);
            blockEntity.load(nativeNbt);
            blockEntity.setChanged();
            return true;
        }
        return false;
    }

    @Override
    public Holder<Biome> getNativeBiome(int biomeX, int biomeY, int biomeZ) {
        return chunk.getNoiseBiome(biomeX, biomeY, biomeZ);
    }

    @Override
    public void setBiome(int biomeX, int biomeY, int biomeZ, Holder<Biome> biome) {
        chunk.setBiome(biomeX, biomeY, biomeZ, biome);
    }

    @Override
    public boolean updateNativeHeightmap(HeightMap heightmap, int x, int y, int z, BlockState state) {
        // move to NativeTypeConversionsImpl?
        Heightmap.Types nativeHeightmap = switch(heightmap) {
            case MOTION_BLOCKING -> Heightmap.Types.MOTION_BLOCKING;
            case MOTION_BLOCKING_NO_LEAVES -> Heightmap.Types.MOTION_BLOCKING_NO_LEAVES;
            case OCEAN_FLOOR -> Heightmap.Types.OCEAN_FLOOR;
            case OCEAN_FLOOR_WG -> Heightmap.Types.OCEAN_FLOOR_WG;
            case WORLD_SURFACE -> Heightmap.Types.WORLD_SURFACE;
            case WORLD_SURFACE_WG -> Heightmap.Types.WORLD_SURFACE_WG;
        };

        return chunk.heightmaps.get(nativeHeightmap).update(x, y, z, state);
    }

    @Override
    public void setUnsaved(boolean needsSaving) {
        chunk.setUnsaved(needsSaving);
    }

    @Override
    public boolean isUnsaved() {
        return chunk.isUnsaved();
    }

    @Override
    public synchronized void removeBlockEntity(int x, int y, int z) {
        chunk.removeBlockEntity(new BlockPos(x, y, z));
    }

    @Override
    public void sendChunkUpdatesPacket(Player plr, boolean modifyBlocks) {
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, chunk.level.getLightEngine(), null, null, modifyBlocks);
        ((CraftPlayer)plr).getHandle().connection.send(packet);
    }

    @Override
    public void broadcastChunkUpdatesPacket() {
        //ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(chunk, chunk.level.getLightEngine(), null, null, modifyBlocks);
        chunk.getChunkHolder().vanillaChunkHolder.broadcastChanges(chunk);
    }

    @Override
    public World getWorld() {
        return chunk.level.getWorld();
    }

    @Override
    public ServerLevel getNativeWorld() {
        return chunk.level;
    }

    @Override
    public int getX() {
        return chunk.locX;
    }

    @Override
    public int getZ() {
        return chunk.locZ;
    }

    @Override
    public LevelChunkSection getNativeSection(int yIndex) {
        return chunk.getSection(yIndex);
    }

    @Override
    public LevelChunkSection[] getNativeSections() {
        return chunk.getSections();
    }

    @Override
    public BlockState getNativeBlockState(int x, int y, int z) {
        return chunk.getBlockState(x, y, z);
    }

    @Override
    public BlockState setNativeBlockState(int x, int y, int z, BlockState nativeState) {
        return chunk.setBlockState(new BlockPos(x, y, z), nativeState, false);
    }

    @Override
    public LevelChunk asNative() {
        return chunk;
    }

    @Override
    public NativeTypeConversionsImpl getConverter() {
        return NativeTypeConversionsImpl.inst();
    }
}
