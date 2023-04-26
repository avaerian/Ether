import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.minerift.ether.nms.MinecraftVersion;

import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
public class MinecraftVersionTest {

    public static class ParseStringState {
        String rawVersion;
        String expectedVersion;

        public ParseStringState(String rawVersion, String expectedVersion) {
            this.rawVersion = rawVersion;
            this.expectedVersion = expectedVersion;
        }
    }

    @ParameterizedTest
    @MethodSource
    public void parseStringVersionTest(ParseStringState state) {

        MinecraftVersion version = new MinecraftVersion(state.rawVersion);
        assertEquals(version.toString(), "(%s)".formatted(state.expectedVersion));

    }

    private static Stream<ParseStringState> parseStringVersionTest() {
        return Stream.of(
                new ParseStringState("(1.9.4)", "1.9.4"),
                new ParseStringState("(1.12.2)", "1.12.2"),
                new ParseStringState("(1.5.4)", "1.5.4"),
                new ParseStringState("MC (1.8.9)", "1.8.9"),
                new ParseStringState("(1.19.3)", "1.19.3")
        );
    }

    @Test
    public void invalidVersionTest() {
        assertThrows(IllegalArgumentException.class,
                () -> new MinecraftVersion("69.420 HA!")
        );
    }

    @Test
    public void sortedComparisonTest() {

        final int MAJOR_VERSION_LIMIT = 100;
        final int MINOR_VERSION_LIMIT = 100;

        MinecraftVersion[] versions = new MinecraftVersion[MAJOR_VERSION_LIMIT * MINOR_VERSION_LIMIT];

        for (int i = 0; i < MAJOR_VERSION_LIMIT; i++) {
            for (int j = 0; j < MINOR_VERSION_LIMIT; j++) {
                MinecraftVersion version = new MinecraftVersion(1, i, j);
                versions[(i * MAJOR_VERSION_LIMIT) + j] = version;
                System.out.println(version + " -> " + version.getCompareScore());
            }
        }

        MinecraftVersion[] sortedVersions = Arrays.copyOf(versions, versions.length);
        Arrays.sort(sortedVersions);

        assertArrayEquals(versions, sortedVersions);
    }

}
