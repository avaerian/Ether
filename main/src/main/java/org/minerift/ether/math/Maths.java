package org.minerift.ether.math;

import com.google.common.base.Function;
import org.minerift.ether.util.Result;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

// Class for all things math-related
public class Maths {

    private final static Pattern VEC_STRING_PATTERN = Pattern.compile("[^\\d|\\-|\\,]");
    private final static Function<Stream<String>, int[]> INT_ARGS_ADAPTER = (stream) -> stream.mapToInt(Integer::parseInt).toArray();
    private final static Function<Stream<String>, double[]> DOUBLE_ARGS_ADAPTER = (stream) -> stream.mapToDouble(Double::parseDouble).toArray();

    // For testing
    public static void main(String[] args) {
        System.out.println(Maths.strToVec3i("    1738,  -69,   420   "));
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

    public static boolean inRangeInclusive(Vec3i minInclusive, Vec3i maxInclusive, Vec3i test) {
        return minInclusive.isLessThan(test, true) && maxInclusive.isGreaterThan(test, true);
    }

    public static boolean inRangeExclusive(Vec3i minInclusive, Vec3i maxExclusive, Vec3i test) {
        return minInclusive.isLessThan(test, true) && maxExclusive.isGreaterThan(test, false);
    }

    private static <T> T strToVecArgs(String str, String type, int expectedArgs, Function<Stream<String>, T> argsAdapter) {

        checkNotNull(str);
        checkArgument(str.isBlank(), String.format("String for %s was blank!", type));

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

}
