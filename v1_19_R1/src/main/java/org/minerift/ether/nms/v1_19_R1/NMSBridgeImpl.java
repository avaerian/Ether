package org.minerift.ether.nms.v1_19_R1;

import ca.spottedleaf.concurrentutil.executor.standard.PrioritisedExecutor;
import ca.spottedleaf.starlight.common.light.StarLightEngine;
import ca.spottedleaf.starlight.common.light.StarLightInterface;
import io.netty.buffer.Unpooled;
import io.papermc.paper.chunk.system.light.LightQueue;
import io.papermc.paper.chunk.system.scheduling.ChunkLightTask;
import io.papermc.paper.chunk.system.scheduling.ChunkTaskScheduler;
import it.unimi.dsi.fastutil.shorts.ShortArrayList;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortCollections;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ThreadedLevelLightEngine;
import net.minecraft.util.datafix.fixes.ChunkLightRemoveFix;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.minerift.ether.nms.NMSBridge;

import java.util.Arrays;
import java.util.Map;

public class NMSBridgeImpl implements NMSBridge {


    @Override
    public void fastClearChunk(Chunk chunk) {

        // Get chunk implementation and empty instance
        final LevelChunk nmsChunk = ((CraftChunk) chunk).getHandle();
        final LevelChunk emptyChunk = new LevelChunk(nmsChunk.getLevel(), nmsChunk.getPos());

        // Write empty chunk section to buffer
        final FriendlyByteBuf emptySectionBuf = new FriendlyByteBuf(Unpooled.buffer());
        emptyChunk.getSection(0).write(emptySectionBuf);

        // Update chunk and sections
        nmsChunk.clearAllBlockEntities();
        ShortCollection sections = new ShortOpenHashSet();
        for(LevelChunkSection section : nmsChunk.getSections()) {
            section.read(emptySectionBuf);
            section.recalcBlockCounts();
            emptySectionBuf.resetReaderIndex();
            sections.add((short)(section.bottomBlockY() >> 4));
        }
        System.out.println("sections: " + Arrays.toString(sections.toShortArray()));

        // TODO: evaluate if this is needed
        nmsChunk.setBlockEmptinessMap(emptyChunk.getBlockEmptinessMap());
        nmsChunk.setSkyEmptinessMap(emptyChunk.getSkyEmptinessMap());
        nmsChunk.setBlockNibbles(emptyChunk.getBlockNibbles());
        nmsChunk.setSkyNibbles(emptyChunk.getSkyNibbles());

        for(Map.Entry<Heightmap.Types, Heightmap> entry : emptyChunk.getHeightmaps()) {
            nmsChunk.setHeightmap(entry.getKey(), entry.getValue().getRawData());
        }

        //relightChunkWithNeighbors(chunk);
        // TODO: investigate buggy shadows around chunk edges

        nmsChunk.setLightCorrect(false);
        relightNMSChunk_New(nmsChunk);

        // TODO: try to reload chunk? try to unload and load chunk (doesnt seem safe -> try reloading)?
        nmsChunk.getLevel().getChunkSource().updateChunkForced(nmsChunk.getPos(), true);

        //nmsChunk.resetNeighbours();
        //nmsChunk.setUnsaved(true);

        // Resend packets to clients
        refreshChunkForClients(chunk);

        /*
            TODO: IMPORTANT NOTES 
            The chunk lighting updates for the server seem fine (maybe see if priority of ChunkLightTask can be lowered).
            The chunk lighting persists when the chunks are initially updated, but when the client
            unloads and reloads the chunks, the chunk lighting is fixed.
            Possible conclusions include needing to force reload the chunk on the server, as well as investigate
            if packets can be deferred until task is complete (looks unlikely due to inaccessibility)

         */

    }

    // First few attempts, which seem to semi-work
    @Deprecated
    public void relightNMSChunk(LevelChunk chunk) {

        final ThreadedLevelLightEngine lightEngine = (ThreadedLevelLightEngine) chunk.getLevel().getLightEngine();
        final StarLightInterface theLightEngine = lightEngine.theLightEngine;
        final LightQueue lightQueue = theLightEngine.lightQueue;

        ShortCollection sections = new ShortOpenHashSet();
        for (LevelChunkSection section : chunk.getSections()) {
            sections.add((short)(section.bottomBlockY() >> 4));
        }

        SectionPos pos = SectionPos.bottomOf(chunk);
        lightQueue.queueChunkBlocklightEdgeCheck(pos, sections);
        lightQueue.queueChunkSkylightEdgeCheck(pos, sections);

    }


    public void relightNMSChunk_New(LevelChunk chunk) {

        final ServerLevel world = chunk.getLevel().getMinecraftWorld();
        final ChunkTaskScheduler scheduler = world.chunkTaskScheduler;

        /*
        final ThreadedLevelLightEngine lightEngine = world.getChunkSource().getLightEngine();
        final StarLightInterface starLightInterface = lightEngine.theLightEngine;
        final LightQueue lightQueue = starLightInterface.lightQueue;
        */

        ChunkLightTask task = new ChunkLightTask(scheduler, world, chunk.locX, chunk.locZ, chunk, PrioritisedExecutor.Priority.BLOCKING);
        task.schedule();

        // LightQueue.queueChunkLightTask(task)
        // LightQueue.setPriority(task.chunkX, task.chunkZ, priority);
    }

    @Deprecated
    public void relightChunkWithNeighbors(Chunk chunk) {

        final LevelChunk nmsChunk = ((CraftChunk) chunk).getHandle();

        final ThreadedLevelLightEngine lightEngine = (ThreadedLevelLightEngine) nmsChunk.getLevel().getLightEngine();
        final LightQueue lightQueue = lightEngine.theLightEngine.lightQueue;

        ShortCollection sections = new ShortOpenHashSet();
        for (LevelChunkSection section : nmsChunk.getSections()) {
            sections.add((short)(section.bottomBlockY() >> 4));
        }

        int[][] chunks = {
                {0, 0},
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        // TODO
        for(int[] offset : chunks) {
            SectionPos pos = SectionPos.bottomOf(nmsChunk);
            lightQueue.queueChunkBlocklightEdgeCheck(pos, sections);
            lightQueue.queueChunkSkylightEdgeCheck(pos, sections);
        }

    }

    @Override
    public void fastClearRegion(Location p1, Location p2) {
        // TODO
    }

    public void refreshChunkForClients(Chunk chunk) {
        chunk.getWorld().refreshChunk(chunk.getX(), chunk.getZ());
    }

    @Deprecated
    // Resends chunk packets to clients
    public void refreshChunksForClients(Chunk chunk) {

        int[][] chunks = {
                {0, 0},
                {1, 0},
                {-1, 0},
                {0, 1},
                {0, -1}
        };

        // Try updating surrounding chunks
        for(int[] offsets : chunks) {
            chunk.getWorld().refreshChunk(chunk.getX() + offsets[0], chunk.getZ() + offsets[1]);
        }

        /*LevelChunk nmsChunk = ((CraftChunk) chunk).getHandle();
        Level level = nmsChunk.getLevel();
        ChunkHolder holder = nmsChunk.getChunkHolder().vanillaChunkHolder;

        ClientboundLevelChunkWithLightPacket packet = new ClientboundLevelChunkWithLightPacket(nmsChunk, level.getLightEngine(), null, null, false, false);
        holder.broadcast(packet, false);*/
    }

    @Deprecated
    public void clearChunkHeightmaps(LevelChunk chunk) {

        for(Map.Entry<Heightmap.Types, Heightmap> entry : chunk.getHeightmaps()) {

            Heightmap.Types type = entry.getKey();
            Heightmap heightmap = entry.getValue();

            // TODO: understand heightmap data structure
            long[] emptyHeightmap = new long[heightmap.getRawData().length];
            heightmap.setRawData(chunk, type, emptyHeightmap);

        }
    }

    @Override
    public void fastSetBlock(Location loc, Block block) {

        LevelChunk nmsChunk = ((CraftChunk) loc.getChunk()).getHandle();
        // Get section (create new if section doesn't exist; else, get)
        LevelChunkSection section = nmsChunk.getSection(loc.getBlockY() >> 4);
        // Set block in section (with bitwise operators to keep position in range 0-15)


    }
}
