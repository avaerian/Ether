package org.minerift.ether.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.util.math.Maths;
import org.minerift.ether.util.math.Vec2i;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GridAlgorithmTest {

    private static final Random RANDOM = new Random();

    // Validate by ensuring the algorithm works from and to tile coordinates and id
    @ParameterizedTest
    @MethodSource
    public void inverseTest(int inputId) {
        final Vec2i tile = GridAlgorithm.computeTile(inputId);
        final int tileId = GridAlgorithm.computeTileId(tile);
        //System.out.println(inputId + " -> " + tile + " -> " + tileId);
        assertEquals(tileId, inputId);
    }

    // Provides parameters for test above
    private static Stream<Integer> inverseTest() {
        // Inclusive
        final int start = 0;
        final int end = 100;
        IntStream stream = IntStream.rangeClosed(start, end);
        return stream.boxed();
    }

    @ParameterizedTest
    @MethodSource
    public void tileFromStringTest(int id) {
        Vec2i tileFromId = GridAlgorithm.computeTile(id);
        String str = tileFromId.toString();
        Vec2i tileFromStr = Maths.strToVec2i(str).getValueOrDefault(() -> null);
        assertEquals(tileFromStr, tileFromId);
        assertEquals(tileFromStr.getTileId(), tileFromId.getTileId());
    }

    @ParameterizedTest
    @MethodSource
    public void random_tileFromStringTest(int id) {
        tileFromStringTest(id);
    }

    private static Stream<Integer> random_tileFromStringTest() {
        // Generate a range of random ints
        final int tileCount = 50;
        final int limit = 50000;
        return RANDOM.ints(tileCount, 0, limit).boxed();
    }

    private static Stream<Integer> tileFromStringTest() {
        return Arrays.stream(new int[]{32645, 12789, 44322, 21897, 3846, 43567}).boxed();
    }

    /*@Test
    public void custom_inverseTest() {
        int[] ids = {32645, 12789, 44322, 21897, 3846, 43567};
        for(int id : ids) {
            System.out.println(GridAlgorithm.computeTile(id));
        }
    }*/

    // Validate by ensuring the algorithm works from and to tile coordinates and id
    // Runs through random values instead of a defined range
    @ParameterizedTest
    @MethodSource
    public void random_inverseTest(int inputId) {
        inverseTest(inputId);
    }

    // Provides random parameters for test above
    private static Stream<Integer> random_inverseTest() {
        final int randomValueCount = 50;
        final int randomBound = 50000;
        return RANDOM.ints(randomValueCount, 0, randomBound).boxed();
    }

    @Test
    public void nullTileTest() {
        assertThrows(
                NullPointerException.class,
                () -> GridAlgorithm.computeTileId(null)
        );
    }

    @Test
    public void outOfRangeTileIdTest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> GridAlgorithm.computeTile(-1)
        );
    }
}
