package org.minerift.ether.nms.v1_20_R2;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.debug.Experiments;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.nms.world.Section;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Experimental
public class ExperimentsImpl implements Experiments {

    @Override
    public void testIslandScanIdea(Location loc) {

        // Hacky way of trying to store int by reference
        final Map<BlockState, int[]> stateCounts = new HashMap<>();

        final Chunk chunk = Chunk.of(loc.getChunk());
        final Section section = chunk.getSection(
                Section.getSectionIdx(loc.getBlockY(), loc.getWorld().getMinHeight()));

        ((LevelChunkSection)section).getStates().forEachLocation((state, _loc) -> {
            int[] count = stateCounts.computeIfAbsent(state, (ignore) -> new int[1]);
            count[0]++;
        });

        stateCounts.forEach((key, value) -> System.out.println(key + " : " + value[0]));
    }

    // Full chunk
    @Override
    public void testIslandScanIdeaFullChunk(Location loc) {

        final Map<BlockState, int[]> stateCounts = new HashMap<>();

        final LevelChunk chunk = (LevelChunk) Chunk.of(loc.getChunk()).asNative();
        for(LevelChunkSection section : chunk.getSections()) {
            section.getStates().forEachLocation((state, _loc) -> {
                int[] count = stateCounts.computeIfAbsent(state, (ignore) -> new int[1]);
                count[0]++;
            });
        }

        stateCounts.forEach((key, value) -> System.out.println(key + " : " + value[0]));
    }

    // Multiple chunks
    // Scanning blocks to calculate island value
    @NeedsTesting
    @Override
    public void testIslandScanIdeaMultiChunk(ChunkGetter cg, Location loc, int diameter) {

        final World world = loc.getWorld();
        final ServerLevel level = ((CraftWorld)world).getHandle();

        final int centerX = loc.getChunk().getX();
        final int centerZ = loc.getChunk().getZ();

        int radius = (diameter - 1) / 2;

        final CompletableFuture<Chunk> cf1 = cg.accept(world, centerX - radius, centerZ - radius, null);
        final CompletableFuture<Chunk> cf2 = cg.accept(world, centerX + radius, centerZ + radius, null);

        CompletableFuture<Object2IntMap<BlockState>> statesf = CompletableFuture.allOf(cf1, cf2).thenApply(__ -> {
            final Object2IntMap<BlockState> stateCounts = new Object2IntOpenHashMap<>();

            final Chunk c1 = cf1.join();
            final Chunk c2 = cf2.join();

            final ChunkPos p1 = ((LevelChunk)c1.asNative()).getPos();
            final ChunkPos p2 = ((LevelChunk)c2.asNative()).getPos();

            ChunkPos.rangeClosed(p1, p2).forEach(pos -> {
                final LevelChunk chunk = level.getChunk(pos.x, pos.z);
                for(LevelChunkSection section : chunk.getSections()) {
                    section.getStates().forEachLocation((state, _loc) -> {
                        stateCounts.mergeInt(state, 1, (v, ___) -> v + 1);
                    });
                }
            });

            for(Object2IntMap.Entry<BlockState> entry : stateCounts.object2IntEntrySet()) {
                System.out.println(entry.getKey() + " : " + entry.getIntValue());
            }

            return stateCounts;
        });
    }

}
