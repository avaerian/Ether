package org.minerift.ether.nms.v1_19_R1;

import com.google.common.collect.AbstractIterator;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.Chunk;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

// TODO: implement later and figure out NMSBridge interface
public class NMSBridgeImpl {

    // Clears a chunk of all blocks/entities
    // Does not perform lighting updates
    private void fastClearSingleChunk(Chunk chunk) {

        final LevelChunk nmsChunk = ((CraftChunk) chunk).getHandle();
        final LevelChunk emptyChunk = new LevelChunk(nmsChunk.getLevel(), nmsChunk.getPos());

        final ChunkPos chunkPos = nmsChunk.getPos();
        final ServerLevel serverLevel = nmsChunk.level;
        final ServerChunkCache serverChunkCache = serverLevel.getChunkSource();

        // Write empty chunk section to buffer
        final FriendlyByteBuf emptySectionBuf = new FriendlyByteBuf(Unpooled.buffer());
        emptyChunk.getSection(0).write(emptySectionBuf);

        // Update chunk and sections
        nmsChunk.clearAllBlockEntities();
        for(LevelChunkSection section : nmsChunk.getSections()) {
            section.read(emptySectionBuf);
            section.recalcBlockCounts();
            emptySectionBuf.resetReaderIndex();
        }
        //System.out.println("sections: " + Arrays.toString(sections.toShortArray()));

        // TODO: evaluate if this is needed
        nmsChunk.setBlockEmptinessMap(emptyChunk.getBlockEmptinessMap());
        nmsChunk.setSkyEmptinessMap(emptyChunk.getSkyEmptinessMap());
        nmsChunk.setBlockNibbles(emptyChunk.getBlockNibbles());
        nmsChunk.setSkyNibbles(emptyChunk.getSkyNibbles());

        // Notify of block updates
        for(final BlockPos blockPos : BlockPos.betweenClosed(chunkPos.getMinBlockX(), serverLevel.getMinBuildHeight(), chunkPos.getMinBlockZ(), chunkPos.getMaxBlockX(), serverLevel.getMaxBuildHeight() - 1, chunkPos.getMaxBlockZ())) {
            serverChunkCache.blockChanged(blockPos);
        }
    }

    public void fastClearChunks(Chunk e1, Chunk e2) {

        if(e1.getWorld() != e2.getWorld()) {
            throw new IllegalArgumentException("Chunks are not in the same world!");
        }

        LevelChunk nmsChunk1 = ((CraftChunk) e1).getHandle();
        LevelChunk nmsChunk2 = ((CraftChunk) e2).getHandle();

        ServerLevel serverLevel = nmsChunk1.level;
        ServerChunkCache serverChunkCache = serverLevel.getChunkSource();

        // TODO: loop and clear chunks individually
        for(ChunkPos chunkPos : betweenChunksClosed(nmsChunk1.getPos(), nmsChunk2.getPos())) {

        }

        serverChunkCache.getLightEngine().relight(getNeighboringChunks(e1, e2), a -> {}, b -> {});

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

    // TODO: figure out
    private Iterable<ChunkPos> betweenChunksClosed(ChunkPos e1, ChunkPos e2) {

        final int chunksX = e2.x - e1.x;
        final int chunksZ = e2.z - e1.z;

        return () -> new AbstractIterator<ChunkPos>() {
            private int x, z;

            @Nullable
            @Override
            protected ChunkPos computeNext() {
                return null;
            }
        };
    }
}
