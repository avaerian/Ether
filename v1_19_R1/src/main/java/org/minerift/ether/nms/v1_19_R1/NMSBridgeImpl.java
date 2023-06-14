package org.minerift.ether.nms.v1_19_R1;

import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.text.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.minerift.ether.nms.NMSBridge;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class NMSBridgeImpl implements NMSBridge {

    private void fastClearSingleChunk(ChunkPos pos, World world, boolean clearEntities) {
        fastClearSingleChunk(world.getChunkAt(pos.x, pos.z), clearEntities);
    }

    // Clears a chunk of all blocks/entities
    // Does not perform lighting updates
    private void fastClearSingleChunk(Chunk chunk, boolean clearEntities) {

        if(!chunk.isLoaded()) {
            chunk.load();
        }

        final LevelChunk nmsChunk = ((CraftChunk) chunk).getHandle();
        final LevelChunk emptyChunk = new LevelChunk(nmsChunk.getLevel(), nmsChunk.getPos());

        final ServerLevel serverLevel = nmsChunk.level;
        final ServerChunkCache serverChunkCache = serverLevel.getChunkSource();

        // Write empty chunk section to buffer
        final FriendlyByteBuf emptySectionBuf = new FriendlyByteBuf(Unpooled.buffer());
        emptyChunk.getSection(0).write(emptySectionBuf);

        // Remove entities from chunk
        if(clearEntities) {
            Arrays.stream(chunk.getEntities())
                    .filter(entity -> entity.getType() != EntityType.PLAYER)
                    .forEach(Entity::remove);
        }

        // Update chunk and sections
        clearAllBlockEntities(nmsChunk);
        for(LevelChunkSection section : nmsChunk.getSections()) {
            section.read(emptySectionBuf);
            section.recalcBlockCounts();
            emptySectionBuf.resetReaderIndex();
        }

        nmsChunk.setBlockEmptinessMap(emptyChunk.getBlockEmptinessMap());
        nmsChunk.setSkyEmptinessMap(emptyChunk.getSkyEmptinessMap());
        nmsChunk.setBlockNibbles(emptyChunk.getBlockNibbles());
        nmsChunk.setSkyNibbles(emptyChunk.getSkyNibbles());

        // Resend entire chunk packet
        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(nmsChunk, serverChunkCache.getLightEngine(), null, null, true, true);
        nmsChunk.getChunkHolder().vanillaChunkHolder.broadcast(packet, false);
    }

    private void clearAllBlockEntities(LevelChunk chunk) {
        Set<BlockPos> pendingBlockEntities = chunk.getBlockEntitiesPos(); // contains pending block entities
        pendingBlockEntities.forEach(chunk::removeBlockEntity);
        chunk.clearAllBlockEntities(); // do rest of the work
    }

    @Override
    public void fastClearChunk(Chunk chunk, boolean clearEntities) {
        fastClearChunks(chunk, chunk, clearEntities);
    }

    @Override
    public void fastClearChunks(Chunk e1, Chunk e2, boolean clearEntities) {
        fastClearChunksLogic(e1, e2, pos -> {
            fastClearSingleChunk(pos, e1.getWorld(), clearEntities);
        });
    }

    @Override
    public void fastClearChunksAsync(Chunk e1, Chunk e2, boolean clearEntities) {
        fastClearChunksLogic(e1, e2, pos -> {
            e1.getWorld().getChunkAtAsync(pos.x, pos.z, true, chunk -> fastClearSingleChunk(chunk, clearEntities));
        });
    }

    @Override
    public void fastSetBlock(Block block, Location location) {

        Preconditions.checkNotNull(block, "Block cannot be null!");
        Preconditions.checkNotNull(location, "Location cannot be null!");

        final int x = location.getBlockX();
        final int y = location.getBlockY();
        final int z = location.getBlockZ();

        //block.getWorld().setBlockData();
        //block.getWorld().getBlockAt()

        final Chunk bukkitChunk = block.getChunk();
        final LevelChunk nmsChunk = ((CraftChunk) bukkitChunk).getHandle();

        final LevelChunkSection section = nmsChunk.getSection(nmsChunk.getSectionIndexFromSectionY(location.getBlockY()));

        // Update block state
        section.setBlockState(x, y, z, ((CraftBlockState) block.getState()).getHandle(), false);

        // TODO: finish

    }

    @Override
    public void fastSetBlocks(Set<Block> blocks, Location location, Location origin) {

        // TODO: reconsider this
        Preconditions.checkNotNull(blocks, "Blocks cannot be null!");
        Preconditions.checkNotNull(location, "Location cannot be null!");
        Preconditions.checkNotNull(origin, "Origin cannot be null!");

        // TODO: finish

    }

    private void fastClearChunksLogic(Chunk e1, Chunk e2, Consumer<ChunkPos> clearChunk) {

        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world!");
        }

        final LevelChunk nmsChunk1 = ((CraftChunk) e1).getHandle();
        final LevelChunk nmsChunk2 = ((CraftChunk) e2).getHandle();

        final ServerLevel serverLevel = nmsChunk1.level;
        final ServerChunkCache serverChunkCache = serverLevel.getChunkSource();

        Bukkit.broadcast(Component.text("Clearing chunk contents..."));
        ChunkPos.rangeClosed(nmsChunk1.getPos(), nmsChunk2.getPos()).forEach(clearChunk);

        Bukkit.broadcast(Component.text("Relighting..."));
        serverChunkCache.getLightEngine().relight(getNeighboringChunks(e1, e2), a -> {}, b -> {});

        Bukkit.broadcast(Component.text(String.format("Cleared %d chunk(s)", ChunkPos.rangeClosed(nmsChunk1.getPos(), nmsChunk2.getPos()).count())));
    }

    // Get the neighboring chunks for a single chunk
    private HashSet<ChunkPos> getNeighboringChunks(Chunk chunk) {
        return getNeighboringChunks(chunk, chunk);
    }

    // Get the neighboring chunks between two endpoints (chunks)
    private HashSet<ChunkPos> getNeighboringChunks(Chunk e1, Chunk e2) {

        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world!");
        }

        int capacity = (e2.getZ() - e1.getZ() + 3) * (e2.getX() - e1.getX() + 3);
        HashSet<ChunkPos> chunks = new HashSet<>(capacity);
        for(int x = e1.getX() - 1; x <= e2.getX() + 1; x++) {
            for(int z = e1.getZ() - 1; z <= e2.getZ() + 1; z++) {
                chunks.add(new ChunkPos(x, z));
                //System.out.println(String.format("%d, %d", x, z)); // debug
            }
        }

        return chunks;
    }
}
