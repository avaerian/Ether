import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.minerift.ether.GridAlgorithm;
import org.minerift.ether.island.Tile;

import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class GridAlgorithmTest {

    private GridAlgorithm algorithm;

    @BeforeAll
    public void setup() {
        this.algorithm = new GridAlgorithm();
    }

    // Validate by ensuring the algorithm works from and to tile coordinates and id
    @ParameterizedTest
    @MethodSource
    public void inverseTest(int inputId) {
        final Tile tile = algorithm.computeTile(inputId);
        final int tileId = algorithm.computeTileId(tile);
        System.out.println(inputId + " -> " + tile + " -> " + tileId);
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
        Random random = new Random();
        return random.ints(randomValueCount, 0, randomBound).boxed();
    }

    @Test
    public void nullTileTest() {
        assertThrows(
                NullPointerException.class,
                () -> algorithm.computeTileId(null)
        );
    }

    @Test
    public void outOfRangeTileIdTest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> algorithm.computeTile(-1)
        );
    }

}
