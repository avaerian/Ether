package org.minerift.ether.schematic.data;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.*;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.work.deprecated.BatchedTask;
import org.minerift.ether.world.BlockEntityArchetype;
import org.minerift.ether.world.ChunkCoords;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.IntConsumer;

import static org.minerift.ether.nms.world.Section.*;
import static org.minerift.ether.schematic.data.Array3DOrder.YZX;

@SuppressWarnings("Duplicates")
public class Pasters {

    public static void pasteBlockVolume(BlockVolume bv, World world, Vec3i loc, ChunkGetter cg) {
        //Vec3i end = loc.copy().copyMutable().add(bv.getDimensions());
        bv.getBlockEntities().forEach((be) -> be.getPos().add(loc));

        Vec2i tr = ChunkCoords.getChunkAt(loc);
        Vec2i tl = ChunkCoords.getChunkAt(loc.copyMutable().add(bv.getWidth(), 0, 0));
        Vec2i bl = ChunkCoords.getChunkAt(loc.copyMutable().add(0, 0, bv.getLength()));
        Vec2i br = ChunkCoords.getChunkAt(loc.copyMutable().add(bv.getDimensions()));

        /*System.out.println("loc: " + loc);
        System.out.println("end: " + end);
        System.out.println("bv dim: " + bv.getDimensions());*/

        System.out.println("tr: " + tr);
        System.out.println("tl: " + tl);
        System.out.println("bl: " + bl);
        System.out.println("br: " + br);

        final int startSecY = getSectionIdx(loc.getY(), world.getMinHeight());
        final int endSecY = getSectionIdx(loc.getY() + bv.getHeight(), world.getMinHeight());

        System.out.println("startSecY: " + startSecY + ", endSecY: " + endSecY);

        // Start at top right
        // TODO: experiment w/ CompletableFuture#allOf for pasting in each chunk (each chunk being locked)
        BatchedTask operation = new BatchedTask();
        for(int cz = tr.getZ(); cz <= br.getZ(); cz++) {
            for(int cx = tr.getX(); cx <= tl.getX(); cx++) {
                int finalCx = cx;
                int finalCz = cz;
                operation.addTask(() -> {
                    cg.getChunkWCallback(world, finalCx, finalCz, (chunk) -> { // returns CompletableFuture
                        for(int sy = startSecY; sy <= endSecY; sy++) {
                            pasteBlockSection(chunk, sy, bv, loc);
                        }
                        chunk.setUnsaved(true);
                        return chunk;
                    });
                });
            }
        }
        operation.whenComplete((status) -> {
            System.out.println(bv.blockEntities);
            //System.out.println(bv.getBlocks().getBlockEntities());
            Bukkit.broadcast(Component.text("finished -> status: " + status));
        });

        Ether.inst().getWorkQueue().enqueue(operation);

        //System.out.println(bv.getBlocks().getData().length);
        //System.out.println("idxs: " + idxs.size());

        //System.out.println(loc);
        //System.out.println(end);

    }

    public static void pasteBlockVolume(BlockVolume bv, World world, Vec3i loc) {
        pasteBlockVolume(bv, world, loc, ChunkGetter.SYNC);
    }

    // add flag for acquiring/releasing chunk sections?
    private static void pasteBlockSection(Chunk chunk, int sy, BlockVolume bv, Vec3i loc) {
        int cx = chunk.getX();
        int cz = chunk.getZ();
        int realSectionY = sectionRealFromIdx(sy, chunk.getWorld().getMinHeight());
        Section section = chunk.getSection(sy);

        int normX = roundChunk(loc.getX());
        int normY = roundChunk(loc.getY());
        int normZ = roundChunk(loc.getZ());

        Vec2i startChunk = ChunkCoords.getChunkAt(loc);
        Vec2i.Mutable normalizedChunk = new Vec2i.Mutable(cx, cz);
        //@Debug System.out.println("Chunk: " + normalizedChunk);
        normalizedChunk.subtract(startChunk);
        //@Debug System.out.println("Normalized: " + normalizedChunk);

        int realStartSecY = getSectionReal(loc.getY());
        int normStartSecY = realSectionY - realStartSecY;
        //System.out.println("normStartSecY: " + normStartSecY);

        //System.out.println(((startSecY - sy) * 16) + " " + roundChunk(loc.getY()));
        Vec3i chunkStart = new Vec3i(
                roundChunk(Math.min(16, normX + (normalizedChunk.getX() * 16))),
                roundChunk(Math.min(16, normY + (normStartSecY * 16))),
                roundChunk(Math.min(16, normZ + (normalizedChunk.getZ() * 16)))
        );

        //System.out.println("Chunk Start: " + chunkStart);

        int startX = chunkStart.getX();
        int startY = chunkStart.getY();
        int startZ = chunkStart.getZ();

        int endX = Math.min(16, normX + bv.getWidth() - (normalizedChunk.getX() * 16));
        int endY = Math.min(16, normY + bv.getHeight() - (normStartSecY * 16));
        int endZ = Math.min(16, normZ + bv.getLength() - (normalizedChunk.getZ() * 16));

        /*System.out.println(format("StartX: %d, EndX: %d, Blocks: %d", startX, endX, endX - startX));
        System.out.println(format("StartY: %d, EndY: %d, Blocks: %d", startY, endY, endY - startY));
        System.out.println(format("StartZ: %d, EndZ: %d, Blocks: %d", startZ, endZ, endZ - startZ));
        */
        //System.out.println("normalizedEndY: " + normalizedEndY);

        ChunkSectionChanges sectionChanges = new ChunkSectionChanges(
                chunk,
                (endX - startX) * (endY - startY) * (endZ - startZ)
        );

        section.acquire();
        try {
            for(int y = startY; y < endY; y++) {
                for(int z = startZ; z < endZ; z++) {
                    for(int x = startX; x < endX; x++) {
                        int arrayBlockX = (normalizedChunk.getX() * 16) + (x - startX) - (normX - chunkStart.getX());
                        int arrayBlockY = (normStartSecY * 16) + (y - startY) - (normY - chunkStart.getY());
                        int arrayBlockZ = (normalizedChunk.getZ() * 16) + (z - startZ) - (normZ - chunkStart.getZ());

                        int worldBlockX = loc.getX() + arrayBlockX;
                        int worldBlockY = loc.getY() + arrayBlockY;
                        int worldBlockZ = loc.getZ() + arrayBlockZ;

                        //@Debug System.out.printf("array: %d, %d, %d\n", arrayBlockX, arrayBlockY, arrayBlockZ);

                        int idx = YZX.flatten(bv.getWidth(), bv.getLength(), arrayBlockX, arrayBlockY, arrayBlockZ);

                        // Set block state
                        BlockState<?> block = bv.getDataAt(idx);
                        BlockState<?> old = section.setBlockState(x, y, z, block);

                        if(old.hasBlockEntity()) {
                            chunk.removeBlockEntity(worldBlockX, worldBlockY, worldBlockZ);
                        }

                        if(block.hasBlockEntity()) {
                            System.out.printf("block entity at %d, %d, %d (%d, %d, %d) -> %d\n",
                                    worldBlockX, worldBlockY, worldBlockZ,
                                    arrayBlockX, arrayBlockY, arrayBlockZ, idx);

                            // experiment with this for stability
                            BlockEntityArchetype be = bv.getBlockEntity(idx);
                            if(be == null) {
                                System.out.println("block entity has no associated archetype");
                            } else {
                                CompoundTag nbt = be.getAsNbt();
                                chunk.setBlockEntity(worldBlockX, worldBlockY, worldBlockZ, block, nbt);
                            }
                        }

                        sectionChanges.add(worldBlockX, worldBlockY, worldBlockZ, block);

                        /*System.out.println(format("world: %d, %d, %d, norm: %d, %d, %d, array: %d, block: %s",
                                worldBlockX, worldBlockY, worldBlockZ,
                                arrayBlockX, arrayBlockY, arrayBlockZ,
                                idx, block));*/

                        chunk.updateHeightmap(HeightMap.MOTION_BLOCKING, x, worldBlockY, z, block);
                        chunk.updateHeightmap(HeightMap.MOTION_BLOCKING_NO_LEAVES, x, worldBlockY, z, block);
                        chunk.updateHeightmap(HeightMap.OCEAN_FLOOR, x, worldBlockY, z, block);
                        chunk.updateHeightmap(HeightMap.WORLD_SURFACE, x, worldBlockY, z, block);
                    }
                }
            }

            //section.updateSectionChanges(sy, sectionChanges); // review
            section.broadcastSectionUpdatesPacket(sectionChanges, true);
        } finally {
            section.release();
        }
    }

    public static int roundChunk(int i) {
        return i & 15;
    }

    public static void pasteBiomeVolume(BiomeVolume bv, World world, Vec3i loc, ChunkGetter cg) {
        //Vec3i end = loc.copy().copyMutable().add(bv.getDimensions());

        Vec2i tr = ChunkCoords.getChunkAt(loc);
        Vec2i tl = ChunkCoords.getChunkAt(loc.copyMutable().add(bv.getWidth(), 0, 0));
        Vec2i bl = ChunkCoords.getChunkAt(loc.copyMutable().add(0, 0, bv.getLength()));
        Vec2i br = ChunkCoords.getChunkAt(loc.copyMutable().add(bv.getDimensions()));

        /*System.out.println("loc: " + loc);
        System.out.println("end: " + end);
        System.out.println("bv dim: " + bv.getDimensions());*/

        System.out.println("tr: " + tr);
        System.out.println("tl: " + tl);
        System.out.println("bl: " + bl);
        System.out.println("br: " + br);

        final int startSecY = getSectionIdx(loc.getY(), world.getMinHeight());
        final int endSecY = getSectionIdx(loc.getY() + bv.getHeight(), world.getMinHeight());

        System.out.println("startSecY: " + startSecY + ", endSecY: " + endSecY);

        List<Chunk> chunks = Collections.synchronizedList(
                new ArrayList<>( (br.getZ() - tr.getZ() + 1) * (tl.getX() - tr.getX() + 1) ));

        // Start at top right
        BatchedTask operation = new BatchedTask();
        for(int cz = tr.getZ(); cz <= br.getZ(); cz++) {
            for(int cx = tr.getX(); cx <= tl.getX(); cx++) {
                int finalCx = cx;
                int finalCz = cz;
                operation.addTask(() -> {
                    cg.getChunkWCallback(world, finalCx, finalCz, (chunk) -> { // returns CompletableFuture
                        for(int sy = startSecY; sy <= endSecY; sy++) {
                            pasteBiomeSection(chunk, sy, bv, loc);
                        }
                        chunk.setUnsaved(true);
                        chunks.add(chunk);
                        return chunk;
                    });
                });
            }
        }
        operation.whenComplete((status) -> {
            Ether.inst().getNms().broadcastChunkBiomeUpdates(world, chunks);
            Bukkit.broadcast(Component.text("finished -> status: " + status));
        });

        Ether.inst().getWorkQueue().enqueue(operation);

        //System.out.println(bv.getBlocks().getData().length);
        //System.out.println("idxs: " + idxs.size());

        //System.out.println(loc);
        //System.out.println(end);

    }

    public static void pasteBiomeVolume(BiomeVolume bv, World world, Vec3i loc) {
        pasteBiomeVolume(bv, world, loc, ChunkGetter.SYNC);
    }

    // add flag for acquiring/releasing chunk sections?
    private static void pasteBiomeSection(Chunk chunk, int sy, BiomeVolume bv, Vec3i loc) {
        int cx = chunk.getX();
        int cz = chunk.getZ();
        int realSectionY = sectionRealFromIdx(sy, chunk.getWorld().getMinHeight());
        Section section = chunk.getSection(sy);

        int normX = roundChunk(loc.getX());
        int normY = roundChunk(loc.getY());
        int normZ = roundChunk(loc.getZ());

        Vec2i startChunk = ChunkCoords.getChunkAt(loc);
        Vec2i.Mutable normalizedChunk = new Vec2i.Mutable(cx, cz);
        normalizedChunk.subtract(startChunk);

        int realStartSecY = getSectionReal(loc.getY());
        int normStartSecY = realSectionY - realStartSecY;

        Vec3i chunkStart = new Vec3i(
                roundChunk(Math.min(16, normX + (normalizedChunk.getX() * 16))),
                roundChunk(Math.min(16, normY + (normStartSecY * 16))),
                roundChunk(Math.min(16, normZ + (normalizedChunk.getZ() * 16)))
        );

        int startX = chunkStart.getX();
        int startY = chunkStart.getY();
        int startZ = chunkStart.getZ();

        int endX = Math.min(16, normX + bv.getWidth() - (normalizedChunk.getX() * 16));
        int endY = Math.min(16, normY + bv.getHeight() - (normStartSecY * 16));
        int endZ = Math.min(16, normZ + bv.getLength() - (normalizedChunk.getZ() * 16));

        section.acquire();
        try {
            for(int y = startY; y < endY; y++) {
                for(int z = startZ; z < endZ; z++) {
                    for(int x = startX; x < endX; x++) {
                        int arrayBlockX = (normalizedChunk.getX() * 16) + (x - startX) - (normX - chunkStart.getX());
                        int arrayBlockY = (normStartSecY * 16) + (y - startY) - (normY - chunkStart.getY());
                        int arrayBlockZ = (normalizedChunk.getZ() * 16) + (z - startZ) - (normZ - chunkStart.getZ());

                        /*int worldBlockX = loc.getX() + arrayBlockX;
                        int worldBlockY = loc.getY() + arrayBlockY;
                        int worldBlockZ = loc.getZ() + arrayBlockZ;*/

                        //@Debug System.out.printf("array: %d, %d, %d\n", arrayBlockX, arrayBlockY, arrayBlockZ);

                        int idx = YZX.flatten(bv.getWidth(), bv.getLength(), arrayBlockX, arrayBlockY, arrayBlockZ);

                        Biome<?> biome = bv.getDataAt(idx);
                        section.setBiome(x >> 2, y >> 2, z >> 2, biome);

                        /*System.out.println(format("world: %d, %d, %d, norm: %d, %d, %d, array: %d, block: %s",
                                worldBlockX, worldBlockY, worldBlockZ,
                                arrayBlockX, arrayBlockY, arrayBlockZ,
                                idx, block));*/
                    }
                }
            }
        } finally {
            section.release();
        }
    }

    public static void pasteSchematic(Schematic schem, World world, Vec3i loc, ChunkGetter cg) {
        //Vec3i end = loc.copy().copyMutable().add(bv.getDimensions());
        schem.getBlocks().getBlockEntities().forEach((be) -> be.getPos().add(loc));

        Vec2i tr = ChunkCoords.getChunkAt(loc);
        Vec2i tl = ChunkCoords.getChunkAt(loc.copyMutable().add(schem.getWidth(), 0, 0));
        Vec2i bl = ChunkCoords.getChunkAt(loc.copyMutable().add(0, 0, schem.getLength()));
        Vec2i br = ChunkCoords.getChunkAt(loc.copyMutable().add(schem.getDimensions()));

        System.out.println("tr: " + tr);
        System.out.println("tl: " + tl);
        System.out.println("bl: " + bl);
        System.out.println("br: " + br);

        final int startSecY = getSectionIdx(loc.getY(), world.getMinHeight());
        final int endSecY = getSectionIdx(loc.getY() + schem.getHeight(), world.getMinHeight());

        //System.out.println("startSecY: " + startSecY + ", endSecY: " + endSecY);

        // TODO: experiment w/ CompletableFuture#allOf for pasting in each chunk (each chunk being locked)
        List<Chunk> chunks = Collections.synchronizedList(
                new ArrayList<>( (br.getZ() - tr.getZ() + 1) * (tl.getX() - tr.getX() + 1) ));

        BatchedTask operation = new BatchedTask();
        for(int cz = tr.getZ(); cz <= br.getZ(); cz++) {
            for(int cx = tr.getX(); cx <= tl.getX(); cx++) {
                int finalCx = cx;
                int finalCz = cz;
                operation.addTask(() -> {
                    cg.getChunkWCallback(world, finalCx, finalCz, (chunk) -> { // returns CompletableFuture
                        for(int sy = startSecY; sy <= endSecY; sy++) {
                            pasteSchematicSection(chunk, sy, schem, loc);
                        }
                        chunks.add(chunk);
                        chunk.setUnsaved(true);
                        return chunk;
                    });
                });
            }
        }
        operation.whenComplete((status) -> {
            Ether.inst().getNms().broadcastChunkBiomeUpdates(world, chunks);

            System.out.println(schem.getBlocks().blockEntities);

            Bukkit.broadcast(Component.text("finished -> status: " + status));
        });

        Ether.inst().getWorkQueue().enqueue(operation);

        //System.out.println(bv.getBlocks().getData().length);
        //System.out.println("idxs: " + idxs.size());

        //System.out.println(loc);
        //System.out.println(end);

    }

    public static void pasteSchematic(Schematic schem, World world, Vec3i loc) {
        pasteSchematic(schem, world, loc, ChunkGetter.SYNC);
    }

    // add flag for acquiring/releasing chunk sections?
    private static void pasteSchematicSection(Chunk chunk, int sy, Schematic schem, Vec3i loc) {
        int cx = chunk.getX();
        int cz = chunk.getZ();
        int realSectionY = sectionRealFromIdx(sy, chunk.getWorld().getMinHeight());
        Section section = chunk.getSection(sy);

        int normX = roundChunk(loc.getX());
        int normY = roundChunk(loc.getY());
        int normZ = roundChunk(loc.getZ());

        Vec2i startChunk = ChunkCoords.getChunkAt(loc);
        Vec2i.Mutable normalizedChunk = new Vec2i.Mutable(cx, cz);
        //@Debug System.out.println("Chunk: " + normalizedChunk);
        normalizedChunk.subtract(startChunk);
        //@Debug System.out.println("Normalized: " + normalizedChunk);

        int realStartSecY = getSectionReal(loc.getY());
        int normStartSecY = realSectionY - realStartSecY;

        Vec3i chunkStart = new Vec3i(
                roundChunk(Math.min(16, normX + (normalizedChunk.getX() * 16))),
                roundChunk(Math.min(16, normY + (normStartSecY * 16))),
                roundChunk(Math.min(16, normZ + (normalizedChunk.getZ() * 16)))
        );

        //System.out.println("Chunk Start: " + chunkStart);

        int startX = chunkStart.getX();
        int startY = chunkStart.getY();
        int startZ = chunkStart.getZ();

        int endX = Math.min(16, normX + schem.getWidth() - (normalizedChunk.getX() * 16));
        int endY = Math.min(16, normY + schem.getHeight() - (normStartSecY * 16));
        int endZ = Math.min(16, normZ + schem.getLength() - (normalizedChunk.getZ() * 16));

        /*System.out.println(format("StartX: %d, EndX: %d, Blocks: %d", startX, endX, endX - startX));
        System.out.println(format("StartY: %d, EndY: %d, Blocks: %d", startY, endY, endY - startY));
        System.out.println(format("StartZ: %d, EndZ: %d, Blocks: %d", startZ, endZ, endZ - startZ));
        */
        //System.out.println("normalizedEndY: " + normalizedEndY);

        ChunkSectionChanges sectionChanges = new ChunkSectionChanges(
                chunk,
                (endX - startX) * (endY - startY) * (endZ - startZ)
        );

        boolean setBiomes = !schem.getBiomes().isEmpty();

        section.acquire();
        try {
            for(int y = startY; y < endY; y++) {
                for(int z = startZ; z < endZ; z++) {
                    for(int x = startX; x < endX; x++) {
                        int arrayBlockX = (normalizedChunk.getX() * 16) + (x - startX) - (normX - chunkStart.getX());
                        int arrayBlockY = (normStartSecY * 16) + (y - startY) - (normY - chunkStart.getY());
                        int arrayBlockZ = (normalizedChunk.getZ() * 16) + (z - startZ) - (normZ - chunkStart.getZ());

                        int worldBlockX = loc.getX() + arrayBlockX;
                        int worldBlockY = loc.getY() + arrayBlockY;
                        int worldBlockZ = loc.getZ() + arrayBlockZ;

                        //@Debug System.out.printf("array: %d, %d, %d\n", arrayBlockX, arrayBlockY, arrayBlockZ);

                        int idx = YZX.flatten(schem.getWidth(), schem.getLength(), arrayBlockX, arrayBlockY, arrayBlockZ);

                        // Set block state
                        BlockState<?> block = schem.getBlocks().getDataAt(idx);
                        BlockState<?> old = section.setBlockState(x, y, z, block);

                        if(old.hasBlockEntity()) {
                            chunk.removeBlockEntity(worldBlockX, worldBlockY, worldBlockZ);
                        }

                        if(block.hasBlockEntity()) {
                            System.out.printf("block entity at %d, %d, %d (%d, %d, %d) -> %d\n",
                                    worldBlockX, worldBlockY, worldBlockZ,
                                    arrayBlockX, arrayBlockY, arrayBlockZ, idx);

                            // experiment with this for stability
                            BlockEntityArchetype be = schem.getBlocks().getBlockEntity(idx);
                            if(be == null) {
                                System.out.println("block entity has no associated archetype");
                            } else {
                                CompoundTag nbt = be.getAsNbt();
                                chunk.setBlockEntity(worldBlockX, worldBlockY, worldBlockZ, block, nbt);
                            }
                        }
                        sectionChanges.add(worldBlockX, worldBlockY, worldBlockZ, block);

                        /*System.out.println(format("world: %d, %d, %d, norm: %d, %d, %d, array: %d, block: %s",
                                worldBlockX, worldBlockY, worldBlockZ,
                                arrayBlockX, arrayBlockY, arrayBlockZ,
                                idx, block));*/

                        chunk.updateHeightmap(HeightMap.MOTION_BLOCKING, x, worldBlockY, z, block);
                        chunk.updateHeightmap(HeightMap.MOTION_BLOCKING_NO_LEAVES, x, worldBlockY, z, block);
                        chunk.updateHeightmap(HeightMap.OCEAN_FLOOR, x, worldBlockY, z, block);
                        chunk.updateHeightmap(HeightMap.WORLD_SURFACE, x, worldBlockY, z, block);

                        // Set biome
                        if(setBiomes) {
                            Biome<?> biome = schem.getBiomes().getDataAt(idx);
                            section.setBiome(x >> 2, y >> 2, z >> 2, biome);
                        }
                    }
                }
            }

            //section.updateSectionChanges(sy, sectionChanges); // review
            section.broadcastSectionUpdatesPacket(sectionChanges, true);
        } finally {
            section.release();
        }
    }
}
