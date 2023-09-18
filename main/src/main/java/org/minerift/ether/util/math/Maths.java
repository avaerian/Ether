package org.minerift.ether.util.math;

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
        Maths.strToVec3i("    1738,  -69,   420   ").handle(System.out::println, (ex) -> { throw ex; });
    }

    // Attempts to parse the string into a vec of 2 ints (Vec2i).
    // Returns an ok result containing the Vec2i if successful.
    // Returns an err result containing an IllegalArgumentException providing fail details.
    public static Result<Vec2i, IllegalArgumentException> strToVec2i(String str) {

        final Result<Vec2i, IllegalArgumentException> result = new Result<>();

        strToVecArgs(str, "Vec2i", 2, INT_ARGS_ADAPTER).handle(
                (args) -> result.ok(new Vec2i(args[0], args[1])),
                result::err
        );

        return result;
    }

    public static Result<Vec3i, IllegalArgumentException> strToVec3i(String str) {

        final Result<Vec3i, IllegalArgumentException> result = new Result<>();

        strToVecArgs(str, "Vec3i", 3, INT_ARGS_ADAPTER).handle(
                (args) -> result.ok(new Vec3i(args[0], args[1], args[2])),
                result::err
        );

        return result;
    }

    public static Result<Vec3d, IllegalArgumentException> strToVec3d(String str) {

        final Result<Vec3d, IllegalArgumentException> result = new Result<>();

        strToVecArgs(str, "Vec3d", 3, DOUBLE_ARGS_ADAPTER).handle(
                (args) -> result.ok(new Vec3d(args[0], args[1], args[2])),
                result::err
        );

        return result;
    }

    private static <T> Result<T, IllegalArgumentException> strToVecArgs(String str, String type, int expectedArgs, Function<Stream<String>, T> argsAdapter) {

        checkNotNull(str);

        final Result<T, IllegalArgumentException> result = new Result<>();

        // Handle empty/blank string
        if(str.isBlank()) {
            return result.err(new IllegalArgumentException(String.format("String for %s was blank!", type)));
        }

        // Clear all additional characters + whitespaces
        StringBuilder sb = new StringBuilder();
        Matcher matcher = VEC_STRING_PATTERN.matcher(str);
        while(matcher.find()) {
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);

        // Split and transform args into appropriate array
        final String[] args = sb.toString().split(",");
        try {
            checkArgument(args.length == expectedArgs, String.format("Found %d arguments; expected %d (type: %s)", args.length, expectedArgs, type));
            final T adaptedArgs = argsAdapter.apply(Arrays.stream(args));
            result.ok(adaptedArgs);
        } catch (IllegalArgumentException ex) {
            result.err(ex);
        }

        return result;
    }

}
