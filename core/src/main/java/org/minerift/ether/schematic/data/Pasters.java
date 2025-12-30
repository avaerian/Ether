package org.minerift.ether.schematic.data;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.nms.world.ChunkSectionChanges;
import org.minerift.ether.nms.world.Section;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.work.deprecated.BatchedTask;
import org.minerift.ether.world.BlockEntityArchetype;
import org.minerift.ether.world.ChunkCoords;

import static org.minerift.ether.nms.world.Section.*;
import static org.minerift.ether.schematic.data.Array3DOrder.YZX;

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
        // TODO: test async
        BatchedTask operation = new BatchedTask();
        for(int cz = tr.getZ(); cz <= br.getZ(); cz++) {
            for(int cx = tr.getX(); cx <= tl.getX(); cx++) {
                int finalCx = cx;
                int finalCz = cz;
                operation.addTask(() -> {
                    cg.getChunkWCallback(world, finalCx, finalCz, (chunk) -> {
                        for(int sy = startSecY; sy <= endSecY; sy++) {
                            pasteSection(chunk, sy, bv, loc);
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
    private static void pasteSection(Chunk chunk, int sy, BlockVolume bv, Vec3i loc) {
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
}
