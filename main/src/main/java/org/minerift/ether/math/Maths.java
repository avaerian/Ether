package org.minerift.ether.math;

import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.world.ChunkCoords;

import java.util.Arrays;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

// Class for all things math-related
public class Maths {

    public static final int TICKS_PER_SEC = 20;

    private static final Pattern VEC_STRING_PATTERN = Pattern.compile("[^\\d\\-,.]");
    private static final Function<Stream<String>, int[]> INT_ARGS_ADAPTER = (stream) -> stream.mapToInt(Integer::parseInt).toArray();
    private static final Function<Stream<String>, double[]> DOUBLE_ARGS_ADAPTER = (stream) -> stream.mapToDouble(Double::parseDouble).toArray();

    // DEBUG
    public static void main(String[] args) {
        System.out.println(Maths.strToVec3i("    1738,  -69,   420   "));
        System.out.println(Maths.strToVec3d(" (420.69, -3.14,   1802)!   "));
        System.out.println(Maths.strToVec2i("Z(420,b-69a)!"));
    }

    public static Vec2i getTileAt(int blockX, int blockZ) {
        final MainConfig config = Ether.getConfig(ConfigType.MAIN);
        int tileX = blockX / config.getTileLengthBlocks();
        int tileZ = blockZ / config.getTileLengthBlocks();
        if(blockX < 0) tileX--;
        if(blockZ < 0) tileZ--;
        return new Vec2i(tileX, tileZ);
    }

    public static Vec2i getTileAt(Vec3i blockPos) {
        return getTileAt(blockPos.getX(), blockPos.getZ());
    }

    public static Vec3i getVec3iAt(Vec2i tile) {
        final MainConfig config = Ether.getConfig(ConfigType.MAIN);
        return new Vec3i(tile.getX() * config.getTileLengthBlocks(), config.getTileHeight(), tile.getZ() * config.getTileLengthBlocks());
    }

    public static boolean inRangeInclusive(Vec3i minInclusive, Vec3i maxInclusive, Vec3i test) {
        return minInclusive.isLessThan(test, true) && maxInclusive.isGreaterThan(test, true);
    }

    public static boolean inRangeExclusive(Vec3i minInclusive, Vec3i maxExclusive, Vec3i test) {
        return minInclusive.isLessThan(test, true) && maxExclusive.isGreaterThan(test, false);
    }

    protected static Vec2i strToVec2i(String str) {
        var args = strToVecArgs(str, "Vec2i", 2, INT_ARGS_ADAPTER);
        return new Vec2i(args[0], args[1]);
    }

    protected static Vec3i strToVec3i(String str) {
        var args = strToVecArgs(str, "Vec3i", 3, INT_ARGS_ADAPTER);
        return new Vec3i(args[0], args[1], args[2]);
    }

    protected static Vec3d strToVec3d(String str) {
        var args = strToVecArgs(str, "Vec3d", 3, DOUBLE_ARGS_ADAPTER);
        return new Vec3d(args[0], args[1], args[2]);
    }

    private static <T> T strToVecArgs(String str, String type, int expectedArgs, Function<Stream<String>, T> argsAdapter) {
        checkNotNull(str);
        checkArgument(!str.isBlank(), String.format("String for %s was blank!", type));

        // Clear all additional characters + whitespaces
        StringBuilder sb = new StringBuilder();
        Matcher matcher = VEC_STRING_PATTERN.matcher(str);
        while(matcher.find()) {
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);

        // Split and transform args into appropriate array
        final String[] args = sb.toString().split(",");
        checkArgument(args.length == expectedArgs, String.format("Found %d arguments; expected %d (type: %s)", args.length, expectedArgs, type));
        return argsAdapter.apply(Arrays.stream(args));
    }

    /**
     * Compact two ints into a long.
     * @param ms int to squeeze into most significant bits
     * @param ls int to squeeze into least significant bits
     * @return compacted ints as single long
     */
    public static long pack(int ms, int ls) {
        return ((long) ms << 32) | (ls & 0xffffffffL);
    }

    /**
     * Compact chunk coords into a long.
     * Z is most significant and X is least significant.
     * @param x chunk x
     * @param z chunk z
     * @return compacted chunk coords as long
     */
    public static long pack(int x, int z, PackingOrder order) {
        return switch (order) {
            case XZ             -> pack(x, z);
            case ZX, MC_CHUNK   -> pack(z, x);
        };
    }

    public static long pack(Vec2i vec, PackingOrder order) {
        return pack(vec.getX(), vec.getZ(), order);
    }

    public static long pack(ChunkCoords coords, PackingOrder order) {
        return pack(coords.x, coords.z, order);
    }

    public static long packAsChunkKey(Vec2i vec) {
        return pack(vec.getX(), vec.getZ(), PackingOrder.ZX);
    }

    public static long packAsChunkKey(ChunkCoords chunk) {
        return pack(chunk.x, chunk.z, PackingOrder.ZX);
    }

    /**
     * Unpack a long into a Vec2i based on packing order.
     * @param packed compacted ints as long
     * @param order packing order of long
     * @return Vec2i of the two ints
     */
    public static Vec2i unpack(long packed, PackingOrder order) {
        int[] unpacked = unpackArray(packed, order);
        return new Vec2i(unpacked[0], unpacked[1]);
    }

    /**
     * Unpack a long into two ints based on a packing order.
     * @param packed compacted ints as long
     * @param order packing order of long
     * @return array of the two ints; first is x, second is z.
     */
    public static int[] unpackArray(long packed, PackingOrder order) {
        return switch(order) {
            case XZ             -> new int[] { (int)(packed >> 32), (int)packed }; // x is shifted, z is downcasted
            case ZX, MC_CHUNK   -> new int[] { (int)packed, (int)(packed >> 32) }; // x is downcasted, z is shifted
        };
    }

    public enum PackingOrder {
        XZ,
        ZX, MC_CHUNK // these two are equivalent
    }
}
