package org.minerift.ether.math;

import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.world.ChunkCoords;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Math.PI;

// Class for all things math-related
public class Maths {

    public static final double TAU = PI * 2;
    public static final int TICKS_PER_SEC = 20;

    private static final Pattern VEC_STRING_PATTERN = Pattern.compile("[^\\d\\-,.]");
    private static final Function<Stream<String>, int[]> INT_ARGS_ADAPTER = (stream) -> stream.mapToInt(Integer::parseInt).toArray();
    private static final Function<Stream<String>, double[]> DOUBLE_ARGS_ADAPTER = (stream) -> stream.mapToDouble(Double::parseDouble).toArray();

    // TODO: move this to MathsTest unit test
    @Debug
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

    public static int clamp(int val, int min, int max) {
        return Math.min(max, Math.max(val, min));
    }

    public static int[] range(int minInclusive, int maxExclusive) {
        return IntStream.range(minInclusive, maxExclusive).toArray();
    }

    public static List<Integer> rangeList(int minInclusive, int maxExclusive) {
        return IntStream.range(minInclusive, maxExclusive).boxed().toList();
    }

    public static boolean inRangeInclusive(int minInclusive, int maxInclusive, int test) {
        return minInclusive <= test && test <= maxInclusive;
    }

    public static boolean inRangeInclusive(double minInclusive, double maxInclusive, double test) {
        return minInclusive <= test && test <= maxInclusive;
    }

    public static boolean inRange(int minInclusive, int maxExclusive, int test) {
        return minInclusive <= test && test < maxExclusive;
    }

    public static boolean inRange(double minInclusive, double maxExclusive, double test) {
        return minInclusive <= test && test < maxExclusive;
    }

    public static boolean inRangeInclusiveI(Vec3 minInclusive, Vec3 maxInclusive, Vec3 test) {
        return inRangeInclusive(minInclusive.getX(), maxInclusive.getX(), test.getX())
                && inRangeInclusive(minInclusive.getY(), maxInclusive.getY(), test.getY())
                && inRangeInclusive(minInclusive.getZ(), maxInclusive.getZ(), test.getZ());
    }

    public static boolean inRangeInclusiveD(Vec3 minInclusive, Vec3 maxInclusive, Vec3 test) {
        return inRangeInclusive(minInclusive.getXd(), maxInclusive.getXd(), test.getXd())
                && inRangeInclusive(minInclusive.getYd(), maxInclusive.getYd(), test.getYd())
                && inRangeInclusive(minInclusive.getZd(), maxInclusive.getZd(), test.getZd());
    }

    public static boolean inRangeI(Vec3 minInclusive, Vec3 maxExclusive, Vec3 test) {
        return inRange(minInclusive.getX(), maxExclusive.getX(), test.getX())
                && inRange(minInclusive.getY(), maxExclusive.getY(), test.getY())
                && inRange(minInclusive.getZ(), maxExclusive.getZ(), test.getZ());
    }

    public static boolean inRangeD(Vec3 minInclusive, Vec3 maxExclusive, Vec3 test) {
        return inRange(minInclusive.getXd(), maxExclusive.getXd(), test.getXd())
                && inRange(minInclusive.getYd(), maxExclusive.getYd(), test.getYd())
                && inRange(minInclusive.getZd(), maxExclusive.getZd(), test.getZd());
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
