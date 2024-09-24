package org.minerift.ether.nms.v1_20_R2;

import io.papermc.paper.chunk.system.scheduling.NewChunkHolder;
import net.kyori.adventure.text.Component;
import net.minecraft.core.SectionPos;
import net.minecraft.network.protocol.game.ClientboundSectionBlocksUpdatePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.minerift.ether.Ether;
import org.minerift.ether.work.TaskBatch;
import org.minerift.ether.world.ChunkGetter;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class PartitionPaster {

    public static void paste(World world, BlockPartition partition) {
        final int chunksPartitioned = partition.getChunkCount();
        final AtomicInteger chunksUpdated = new AtomicInteger();
        final ChunkGetter chunkGetter = ChunkGetter.ASYNC;

        TaskBatch operation = new TaskBatch();

        partition.forEach((chunkCoords, sections) -> {

            operation.addTask(() -> {
                // Schedule chunk getter and run callback once chunk is found
                chunkGetter.accept(world, chunkCoords.x, chunkCoords.z, (bukkitChunk) -> {
                    ChunkAccess chunk = ((CraftChunk)bukkitChunk).getHandle(ChunkStatus.FULL);
                    for(ChunkSectionChanges sectionChanges : sections.values()) {
                        // Apply blocks
                        LevelChunkSection section = chunk.getSection(sectionChanges.sectionIdx);
                        NMSBridgeImpl.applyBlocksToSection(chunk, section, sectionChanges.blocks);

                        // Prepare packet data
                        ChunkSectionChanges.PacketData packetData = sectionChanges.computePacketData();
                        int bottomBlockY = sectionChanges.sectionIdx << 4;
                        SectionPos sectionPos = SectionPos.of(chunkCoords.x, bottomBlockY, chunkCoords.z);
                        ClientboundSectionBlocksUpdatePacket packet = new ClientboundSectionBlocksUpdatePacket(sectionPos, packetData.positions, packetData.states);

                        // Broadcast section update packet
                        NewChunkHolder chunkHolder = ((LevelChunk)chunk).getChunkHolder(); // chunk should always be loaded so this cast should be fine
                        chunkHolder.vanillaChunkHolder.broadcast(packet, false);

                        chunk.setUnsaved(true); // in case a section fails to update, chunk should still be saved
                    }

                    // Complete callbacks if all blocks have been updated
                    if(chunksUpdated.incrementAndGet() == chunksPartitioned) {

                        Bukkit.broadcast(Component.text("Pasted and relighting..."));

                        // Queue light updates after everything is updated
                        final ServerLevel level = ((CraftWorld) world).getHandle();
                        level.getChunkSource().getLightEngine().relight(partition.getChunksForRelighting().map((pos) -> new ChunkPos(pos.x, pos.z)).collect(Collectors.toSet()), a -> {}, b -> {});
                    }
                });

                // task ran successfully, i guess
                return true;
            });

        });

        Ether.getWorkQueue().enqueue(operation);
    }

}
