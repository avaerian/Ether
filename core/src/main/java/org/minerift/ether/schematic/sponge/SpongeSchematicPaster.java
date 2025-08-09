package org.minerift.ether.schematic.sponge;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.HeightMap;
import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.*;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.SchematicPaster;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.work.BatchedTask;
import org.minerift.ether.world.ChunkCoords;

import static java.lang.String.format;
import static org.minerift.ether.schematic.data.Array3DOrder.YZX;

@SuppressWarnings("Duplicates")
public class SpongeSchematicPaster implements SchematicPaster<SpongeSchematic> {
    @Override
    public void paste(SpongeSchematic schem, Vec3i pasteLoc, String worldName, SchematicPasteOptions options) {
        World world = Bukkit.getWorld(worldName);
        ChunkGetter chunkGetter = ChunkGetter.SYNC; // TODO: make this an option

        Vec3i end = pasteLoc.copy().asMutable().add(schem.getDimensions());
        schem.getBlocks().getBlockEntities().forEach((be) -> be.getPos().add(pasteLoc));

        ChunkCoords tr = new ChunkCoords(pasteLoc);
        ChunkCoords tl = new ChunkCoords(pasteLoc.asMutable().add(schem.getWidth(), 0, 0));
        ChunkCoords bl = new ChunkCoords(pasteLoc.asMutable().add(0, 0, schem.getLength()));
        ChunkCoords br = new ChunkCoords(pasteLoc.asMutable().add(schem.getDimensions()));

        /*System.out.println("pasteLoc: " + pasteLoc);
        System.out.println("end: " + end);
        System.out.println("schem dim: " + schem.getDimensions());*/

        System.out.println("tr: " + tr);
        System.out.println("tl: " + tl);
        System.out.println("bl: " + bl);
        System.out.println("br: " + br);

        final int startSecY = getSectionIdx(pasteLoc.getY());
        final int endSecY = getSectionIdx(pasteLoc.getY() + schem.getHeight());

        System.out.println("startSecY: " + startSecY + ", endSecY: " + endSecY);

        // Start at top right
        // TODO: test async
        BatchedTask operation = new BatchedTask();
        for(int cz = tr.z; cz <= br.z; cz++) {
            for(int cx = tr.x; cx <= tl.x; cx++) {
                int finalCx = cx;
                int finalCz = cz;
                operation.addTask(() -> {
                    chunkGetter.accept(world, finalCx, finalCz, (chunk) -> {
                        for(int sy = startSecY; sy <= endSecY; sy++) {
                            pasteSection(chunk, sy, schem, pasteLoc);
                        }
                        chunk.setUnsaved(true);
                    });
                });
            }
        }
        operation.whenComplete((status) -> {
            System.out.println(schem.getBlocks().blockEntities);
            //System.out.println(schem.getBlocks().getBlockEntities());
            Bukkit.broadcast(Component.text("finished -> status: " + status));
        });

        // TODO: rework work queue system entirely
        Ether.getWorkQueue().enqueue(operation);

        //System.out.println(schem.getBlocks().getData().length);
        //System.out.println("idxs: " + idxs.size());

        //System.out.println(pasteLoc);
        //System.out.println(end);

    }

    public static final int MIN_WORLD_HEIGHT = -64; // temp

    public static int getSectionIdx(int blockY) {
        return (blockY >> 4) - (MIN_WORLD_HEIGHT >> 4);
    }

    public static int sectionRealFromIdx(int sectionIdx) {
        return sectionIdx + (MIN_WORLD_HEIGHT >> 4);
    }

    public static int getSectionReal(int blockY) {
        return (blockY >> 4);
    }

    // TODO: decouple BlockVolume paster from SpongeSchematic (simple); biome paster
    public static void pasteSection(Chunk chunk, int sy, SpongeSchematic schem, Vec3i pasteLoc) {
        int cx = chunk.getX();
        int cz = chunk.getZ();
        int realSectionY = sectionRealFromIdx(sy);
        Section section = chunk.getSection(sy);

        int normX = roundChunk(pasteLoc.getX());
        int normY = roundChunk(pasteLoc.getY());
        int normZ = roundChunk(pasteLoc.getZ());

        Vec2i startChunk = ChunkCoords.getChunkAt(pasteLoc);
        Vec2i.Mutable normalizedChunk = new Vec2i.Mutable(cx, cz);
        //System.out.println("Chunk: " + normalizedChunk);
        normalizedChunk.subtract(startChunk);
        //System.out.println("Normalized: " + normalizedChunk);

        int realStartSecY = getSectionReal(pasteLoc.getY());
        int normStartSecY = realSectionY - realStartSecY;
        //System.out.println("normStartSecY: " + normStartSecY);

        //System.out.println(((startSecY - sy) * 16) + " " + roundChunk(pasteLoc.getY()));
        Vec3i chunkStart = new Vec3i(
                roundChunk(Math.min(16, normX + (normalizedChunk.getX() * 16))),
                roundChunk(Math.min(16, normY + (normStartSecY * 16))),
                roundChunk(Math.min(16, normZ + (normalizedChunk.getZ() * 16)))
        );

        //System.out.println("Chunk Start: " + chunkStart);

        int startX = chunkStart.getX();
        int startY = chunkStart.getY();
        int startZ = chunkStart.getZ();

        /*int endX = Math.min(16, roundChunk(pasteLoc.getX()) + schem.getWidth()  - (normalizedChunk.getX() * 16));
        int endY = Math.min(16, roundChunk(pasteLoc.getY()) + schem.getHeight() - (normStartSecY * 16));
        int endZ = Math.min(16, roundChunk(pasteLoc.getZ()) + schem.getLength() - (normalizedChunk.getZ() * 16));*/
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

        section.acquire();
        try {

            for(int y = startY; y < endY; y++) {
                for(int z = startZ; z < endZ; z++) {
                    for(int x = startX; x < endX; x++) {
                        int arrayBlockX = (normalizedChunk.getX() * 16) + (x - startX) - (normX - chunkStart.getX());
                        int arrayBlockY = (normStartSecY * 16) + (y - startY) - (normY - chunkStart.getY());
                        int arrayBlockZ = (normalizedChunk.getZ() * 16) + (z - startZ) - (normZ - chunkStart.getZ());

                        int worldBlockX = pasteLoc.getX() + arrayBlockX;
                        int worldBlockY = pasteLoc.getY() + arrayBlockY;
                        int worldBlockZ = pasteLoc.getZ() + arrayBlockZ;

                        //System.out.println(format("array: %d, %d, %d", arrayBlockX, arrayBlockY, arrayBlockZ));
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
                            CompoundTag nbt = schem.getBlocks().getBlockEntity(idx).getNbtData();
                            chunk.setBlockEntity(worldBlockX, worldBlockY, worldBlockZ, block, nbt);
                        }

                        sectionChanges.add(worldBlockX, worldBlockY, worldBlockZ, block);

                        //idxs.add(idx); // FIXME: for testing
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

            //section.updateSectionChanges(sy, sectionChanges); // TODO: review
            section.broadcastSectionUpdatesPacket(sectionChanges, true);
        } finally {
            section.release();
        }
    }

    public static int roundChunk(int i) {
        //return i >= 0 ? i & 15 : 15-(~i&15);
        return i & 15;
    }
}
