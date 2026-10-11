package org.minerift.ether.world;

import org.minerift.ether.math.*;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// Represents generic chunk coordinates across versions
// should review this class
public class ChunkCoords {

    // Util class helpful for doing math on regions of chunks
    public static class RegionNormalizedInfo {
        public final int lenX, lenZ, incrX, incrZ;

        public RegionNormalizedInfo(Vec2i pos1, Vec2i pos2) {
            this.lenX = Math.abs(pos1.getX() - pos2.getX()) + 1;
            this.lenZ = Math.abs(pos1.getZ() - pos2.getZ()) + 1;
            this.incrX = pos1.getX() < pos2.getX() ? 1 : -1;
            this.incrZ = pos1.getZ() < pos2.getZ() ? 1 : -1;
        }
    }

    public static Vec2i getChunkAt(int blockX, int blockZ) {
        return new Vec2i(blockX >> 4, blockZ >> 4);
    }

    public static Vec2i getChunkAt(Vec3 blockPos) {
        return getChunkAt(blockPos.getX(), blockPos.getZ());
    }

    public static Vec3i getBlockAt(int chunkX, int chunkZ) {
        return getBlockAt(chunkX, chunkZ, 0);
    }

    public static Vec3i getBlockAt(int chunkX, int chunkZ, int y) {
        return new Vec3i(chunkX << 4, y, chunkZ << 4);
    }

    public static Vec3i getBlockAt(Vec2 chunkPos, int y) {
        return getBlockAt(chunkPos.getX(), chunkPos.getZ(), y);
    }

    public static Vec3i getBlockAt(Vec2 chunkPos) {
        return getBlockAt(chunkPos.getX(), chunkPos.getZ(), 0);
    }

    public static Stream<Vec2i> getNeighboringChunks(Vec2i pos1, Vec2i pos2) {
        RegionNormalizedInfo reg = new RegionNormalizedInfo(pos1, pos2);
        Vec2i outer1 = new Vec2i(pos1.getX() - reg.incrX, pos1.getZ() - reg.incrZ);
        Vec2i outer2 = new Vec2i(pos2.getX() + reg.incrX, pos2.getZ() + reg.incrZ);
        return rangeClosed(outer1, outer2);
    }

    public static Stream<Vec2i> rangeClosed(Vec2i center, int radius) {
        return rangeClosed(
                new Vec2i(center.getX() - radius, center.getZ() - radius),
                new Vec2i(center.getX() + radius, center.getZ() + radius)
        );
    }

    public static Stream<Vec2i> rangeClosed(Vec2i pos1, Vec2i pos2) {
        RegionNormalizedInfo reg = new RegionNormalizedInfo(pos1, pos2);

        // TODO: review mutable vec in spliterator
        return StreamSupport.stream(new Spliterators.AbstractSpliterator<>((long) reg.lenX * reg.lenZ, Spliterator.SIZED) {
            private Vec2i.Mutable curr;

            @Override
            public boolean tryAdvance(Consumer<? super Vec2i> action) {
                // Update position
                if (curr == null) {
                    curr = pos1.copyMutable();
                } else {

                    if (curr.getX() == pos2.getX()) {
                        if (curr.getZ() == pos2.getZ()) {
                            // reached end chunk
                            return false;
                        }

                        // update z
                        curr.set(curr.getX(), curr.getZ() + reg.incrZ);
                    } else {
                        // update x
                        curr.set(curr.getX() + reg.incrX, curr.getZ());
                    }
                }

                // Perform action
                action.accept(curr);
                return true;
            }

        }, false);
    }

    public static long getChunkKey(int x, int z) {
        return Maths.pack(x, z, Maths.PackingOrder.ZX);
    }

    public static long getChunkKey(Vec2i cpos) {
        return getChunkKey(cpos.getX(), cpos.getZ());
    }
}
