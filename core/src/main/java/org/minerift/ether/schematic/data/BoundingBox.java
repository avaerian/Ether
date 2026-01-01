package org.minerift.ether.schematic.data;

import com.google.common.base.Preconditions;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.nms.world.Section;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.world.BlockEntityArchetype;
import org.minerift.ether.world.ChunkCoords;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;

import static java.lang.Math.min;
import static java.util.concurrent.CompletableFuture.allOf;
import static org.minerift.ether.nms.world.Section.*;
import static org.minerift.ether.nms.world.Section.getSectionIdx;
import static org.minerift.ether.schematic.data.Array3DOrder.YZX;

public class BoundingBox {

    public final Vec3i min, max, dim; // review public access; no reason not to?

    public BoundingBox(Vec3i _min, Vec3i _max) {
        this.min = Vec3i.min(_min, _max);
        this.max = Vec3i.max(_min, _max);
        this.dim = new Vec3i(
                max.getX() - min.getX(),
                max.getY() - min.getY(),
                max.getZ() - min.getZ()
        );
    }

    public CompletableFuture<BlockVolume> getBlocks(World world, Array3DOrder order, ChunkGetter cg) {
        return getBlocksMut(world, order, cg, null).thenApply(BlockVolume.Builder::build);
    }

    public CompletableFuture<BlockVolume.Builder> getBlocksMut(World world, Array3DOrder order, ChunkGetter cg, @Nullable ExecutorService exec) {
        Preconditions.checkState(min.getY() >= world.getMinHeight());
        Preconditions.checkState(max.getY() <= world.getMaxHeight());

        byte[] data = new byte[dim.getX() * dim.getY() * dim.getZ()];
        BlockVolume.Builder bv = BlockVolume.builder()
                .setDimensions(dim)
                .setOrder(order)
                .setData(data)
                .setPalette(BytePalette.of());

        // iterate through region
        // if state doesn't exist, get nextId and increment
        // set data at flattened idx
        Vec2i cmin = ChunkCoords.getChunkAt(min.getX(), min.getZ());
        Vec2i cmax = ChunkCoords.getChunkAt(max.getX(), max.getZ());

        int ccount = ((cmax.getX() - cmin.getX()) + 1) * ((cmax.getZ() - cmin.getZ() + 1));

        final int startSecY = getSectionIdx(min.getY(), world.getMinHeight());
        final int endSecY = getSectionIdx(max.getY(), world.getMinHeight());

        BytePalette<BlockState<?>> pltSyncView = BytePalette.synchronize(bv.palette); // create synchronized view of palette
        bv.setPalette(pltSyncView); // set to sections accessing bv can access this

        final Consumer<Chunk> fn = (c) -> {
            synchronized (c.asNative()) { // lock sections as well/instead?
                for(int sy = startSecY; sy <= endSecY; sy++) {
                    accessSection(c, sy, bv, min);
                }
            }
        };

        // TODO: create wrapper for this task to allow for easier management
        final Function<CompletableFuture<Chunk>, CompletableFuture<Void>> run = switch (cg) {
            case SYNC -> (cf) -> cf.thenAccept(fn);
            case ASYNC -> (cf) -> cf.thenAcceptAsync(fn, exec == null ? cf.defaultExecutor() : exec);
        };

        // will asynchronously access native resources; this should be okay due
        // to both the chunk locking (via the synchronized block) and the fact we are
        // purely reading the chunk block states
        CompletableFuture<Void>[] fchunks = new CompletableFuture[ccount];
        for(int i = 0, cz = cmin.getZ(); cz <= cmax.getZ(); cz++) {
            for(int cx = cmin.getX(); cx <= cmax.getX(); cx++, i++) {

                fchunks[i] = run.apply(cg.getChunk(world, cx, cz));
                        /*.exceptionally((ex) -> {
                    exec.shutdownNow();
                    return null;
                });*/ // review vs exceptionallyAsync?
            }
        }

        return allOf(fchunks).thenApply( (__) ->
                bv.setPalette(((BytePalette.Synchronized<BlockState<?>>) bv.palette).unsync()) )
                .thenApply((b) -> { System.out.println("palette: " + b.palette); return b; }); // debug
    }

    /*public static class BBRetrievalTask<V extends Volume<?>> {

    }*/

    private static void accessSection(Chunk chunk, int sy, BlockVolume.Builder bv, Vec3i min) {
        int cx = chunk.getX();
        int cz = chunk.getZ();
        int realSectionY = sectionRealFromIdx(sy, chunk.getWorld().getMinHeight());
        Section section = chunk.getSection(sy);

        int normX = roundChunk(min.getX());
        int normY = roundChunk(min.getY());
        int normZ = roundChunk(min.getZ());

        Vec2i startChunk = ChunkCoords.getChunkAt(min);
        Vec2i.Mutable normalizedChunk = new Vec2i.Mutable(cx, cz);
        normalizedChunk.subtract(startChunk);

        int realStartSecY = getSectionReal(min.getY());
        int normStartSecY = realSectionY - realStartSecY;

        Vec3i chunkStart = new Vec3i(
                roundChunk(min(16, normX + (normalizedChunk.getX() * 16))),
                roundChunk(min(16, normY + (normStartSecY * 16))),
                roundChunk(min(16, normZ + (normalizedChunk.getZ() * 16)))
        );

        int startX = chunkStart.getX();
        int startY = chunkStart.getY();
        int startZ = chunkStart.getZ();

        int endX = min(16, normX + bv.getWidth() - (normalizedChunk.getX() * 16));
        int endY = min(16, normY + bv.getHeight() - (normStartSecY * 16));
        int endZ = min(16, normZ + bv.getLength() - (normalizedChunk.getZ() * 16));

        section.acquire();
        try {
            for(int y = startY; y < endY; y++) {
                for(int z = startZ; z < endZ; z++) {
                    for(int x = startX; x < endX; x++) {
                        int arrayBlockX = (normalizedChunk.getX() * 16) + (x - startX) - (normX - chunkStart.getX());
                        int arrayBlockY = (normStartSecY * 16) + (y - startY) - (normY - chunkStart.getY());
                        int arrayBlockZ = (normalizedChunk.getZ() * 16) + (z - startZ) - (normZ - chunkStart.getZ());

                        int worldBlockX = min.getX() + arrayBlockX;
                        int worldBlockY = min.getY() + arrayBlockY;
                        int worldBlockZ = min.getZ() + arrayBlockZ;

                        int idx = YZX.flatten(bv.getWidth(), bv.getLength(), arrayBlockX, arrayBlockY, arrayBlockZ);

                        // TODO: refactor this to use a context (can create one and update it each iteration
                        //  rather than create a new one every iteration) for saving BlockVolume's, BiomeVolume's,
                        //  entities, and Schematics. Entities may be different when saving; this may require
                        //  NMS for an easier, faster time, but the Context<V> would allow for one function
                        //  with many different lambdas. Will try and dedup other classes copying and pasting
                        //  this same type of code.
                        BlockState<?> state = section.getBlockState(x, y, z);
                        //System.out.println("state -> " + state.asNative());
                        byte id;
                        if(!bv.palette.containsValue(state)) {
                            id = bv.palette.nextId();
                            bv.palette.add(id, state);
                            System.out.println("state -> " + state.asNative());
                        } else {
                            id = bv.palette.getKey(state);
                        }
                        bv.data[idx] = id;

                        if(state.hasBlockEntity()) {
                            System.out.printf("block entity at %d, %d, %d (%d, %d, %d) -> %d\n",
                                    worldBlockX, worldBlockY, worldBlockZ,
                                    arrayBlockX, arrayBlockY, arrayBlockZ, idx);

                            bv.addBlockEntity(new BlockEntityArchetype(state, new Vec3i(arrayBlockX,
                            arrayBlockY,arrayBlockZ), state.propsAsNbt()
                            ));
                        }
                    }
                }
            }
        } finally {
            section.release();
            System.out.println("updated -> " + bv.palette);
        }
    }


    public static int roundChunk(int i) {
        return i & 15;
    }


    @Experimental
    @NeedsTesting
    public CompletableFuture<Chunk[]> getOverlappingChunks(World world, ChunkGetter cg) {
        Vec2i min = ChunkCoords.getChunkAt(this.min.getX(), this.min.getZ());
        Vec2i max = ChunkCoords.getChunkAt(this.max.getX(), this.max.getZ());
        int count = (max.getX() - min.getX()) * (max.getZ() - min.getZ());
        CompletableFuture<Void>[] futures = new CompletableFuture[count];
        AtomicInteger i_ = new AtomicInteger();
        int i = 0;
        Chunk[] chunks = new Chunk[count];
        for(int z = min.getZ(); z < max.getZ(); z++) {
            for(int x = min.getX(); x < max.getX(); x++) {
                futures[i++] = cg.getChunk(world, x, z).thenAccept((c) -> chunks[i_.getAndIncrement()] = c);
            }
        }
        return allOf(futures).thenApply((__) -> chunks);
    }

    // TODO
    public Iterator<BlockState<?>> blkstIter() {
        throw new UnreachableException("unimplemented");
    }

}
