package org.minerift.ether.world;

import com.google.common.base.Objects;
import org.minerift.ether.math.*;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

// Represents generic chunk coordinates across versions
// should review this class
public class ChunkCoords {

    // Util class helpful for doing math on regions of chunks
    public static class RegionNormalizedInfo {
        public final int lenX, lenZ, incrX, incrZ;

        public RegionNormalizedInfo(ChunkCoords pos1, ChunkCoords pos2) {
            this.lenX = Math.abs(pos1.x - pos2.x) + 1;
            this.lenZ = Math.abs(pos1.z - pos2.z) + 1;
            this.incrX = pos1.x < pos2.x ? 1 : -1;
            this.incrZ = pos1.z < pos2.z ? 1 : -1;
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

    public static Stream<ChunkCoords> getNeighboringChunks(ChunkCoords pos1, ChunkCoords pos2) {
        RegionNormalizedInfo reg = new RegionNormalizedInfo(pos1, pos2);
        ChunkCoords outer1 = new ChunkCoords(pos1.x - reg.incrX, pos1.z - reg.incrZ);
        ChunkCoords outer2 = new ChunkCoords(pos2.x + reg.incrX, pos2.z + reg.incrZ);
        return rangeClosed(outer1, outer2);
    }

    public static Stream<ChunkCoords> rangeClosed(ChunkCoords center, int radius) {
        return rangeClosed(new ChunkCoords(center.x - radius, center.z - radius), new ChunkCoords(center.x + radius, center.z + radius));
    }

    public static Stream<ChunkCoords> rangeClosed(ChunkCoords pos1, ChunkCoords pos2) {
        RegionNormalizedInfo reg = new RegionNormalizedInfo(pos1, pos2);

        return StreamSupport.stream(new Spliterators.AbstractSpliterator<>((long) reg.lenX * reg.lenZ, Spliterator.SIZED) {
            private ChunkCoords curr;

            @Override
            public boolean tryAdvance(Consumer<? super ChunkCoords> action) {
                // Update position
                if (curr == null) {
                    curr = pos1;
                } else {

                    if (curr.x == pos2.x) {
                        if (curr.z == pos2.z) {
                            // reached end chunk
                            return false;
                        }

                        // update z
                        curr = new ChunkCoords(curr.x, curr.z + reg.incrZ);
                    } else {
                        // update x
                        curr = new ChunkCoords(curr.x + reg.incrX, curr.z);
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

    public final int x, z;

    public ChunkCoords(int chunkX, int chunkZ) {
        this.x = chunkX;
        this.z = chunkZ;
    }

    public ChunkCoords(Vec2i chunkPos) {
        this(chunkPos.getX(), chunkPos.getZ());
    }

    public ChunkCoords(Vec3i blockPos) {
        this(blockPos.getX() >> 4, blockPos.getZ() >> 4);
    }

    public ChunkCoords(long keyZX) {
        int[] xz = Maths.unpackArray(keyZX, Maths.PackingOrder.ZX);
        this.x = xz[0];
        this.z = xz[1];
    }

    public long getChunkKey() {
        return getChunkKey(x, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChunkCoords that = (ChunkCoords) o;
        return x == that.x && z == that.z;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(x, z);
    }

    // Intended for converting between generic and native ChunkPos types
    public <T> T asNativeType(BiFunction<Integer, Integer, T> makeNativeFunc) {
        return makeNativeFunc.apply(x, z);
    }

    @Override
    public String toString() {
        return "ChunkCoords{" +
                "x=" + x +
                ", z=" + z +
                '}';
    }
}
