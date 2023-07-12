package org.minerift.ether.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.minerift.ether.nms.MinecraftVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class MinecraftVersionTest {

    public static class ParseStringTestState {
        String rawVersion;
        String expectedVersion;

        public ParseStringTestState(String rawVersion, String expectedVersion) {
            this.rawVersion = rawVersion;
            this.expectedVersion = expectedVersion;
        }
    }

    @ParameterizedTest
    @MethodSource
    public void parseStringVersionTest(ParseStringTestState state) {

        MinecraftVersion version = new MinecraftVersion(state.rawVersion);
        assertEquals(version.toString(), state.expectedVersion);

    }

    private static Stream<ParseStringTestState> parseStringVersionTest() {
        return Stream.of(
                new ParseStringTestState("(1.9.4)", "1.9.4"),
                new ParseStringTestState("(1.12.2)", "1.12.2"),
                new ParseStringTestState("(1.5.4)", "1.5.4"),
                new ParseStringTestState("MC (1.8.9)", "1.8.9"),
                new ParseStringTestState("(1.19.3)", "1.19.3"),
                new ParseStringTestState("1.9", "1.9.0")
        );
    }

    @Test
    public void invalidVersionTest() {
        assertThrows(IllegalArgumentException.class,
                () -> new MinecraftVersion("10.2.5.69.420 HA!")
        );
    }

    public static class ComparisonTestState {
        Predicate<MinecraftVersion> predicate;
        MinecraftVersion other;
        boolean isTruthful;
        public ComparisonTestState(Predicate<MinecraftVersion> predicate, MinecraftVersion other, boolean isTruthful) {
            this.predicate = predicate;
            this.other = other;
            this.isTruthful = isTruthful;
        }
    }

    @ParameterizedTest
    @MethodSource
    public void versionComparisonTest(ComparisonTestState state) {

        boolean compare = state.predicate.test(state.other);
        boolean actual = state.isTruthful;

        assertEquals(compare, actual);
    }

    private static Stream<ComparisonTestState> versionComparisonTest() {

        MinecraftVersion V1_8_9 = new MinecraftVersion("1.8.9");
        MinecraftVersion V1_11_1 = new MinecraftVersion("1.11.1");
        MinecraftVersion V4_20_0 = new MinecraftVersion("4.20.0");
        MinecraftVersion V2_1_3 = new MinecraftVersion("2.1.3");

        return Stream.of(
                new ComparisonTestState(V1_8_9::isLessThan, V1_11_1, true),
                new ComparisonTestState(V1_11_1::isGreaterThan, V4_20_0, false),
                new ComparisonTestState(V2_1_3::isLessThan, V4_20_0, true),
                new ComparisonTestState(V2_1_3::isGreaterThan, V1_8_9, true),
                new ComparisonTestState(V4_20_0::isGreaterThan, V1_11_1, true),
                new ComparisonTestState(V4_20_0::isLessThan, V2_1_3, false)
        );
    }

    @Test
    public void sortedComparisonTest() {

        final int MINOR_VERSION_LIMIT = 100;
        final int PATCH_VERSION_LIMIT = 100;

        MinecraftVersion[] versions = new MinecraftVersion[MINOR_VERSION_LIMIT * PATCH_VERSION_LIMIT];

        for (int i = 0; i < MINOR_VERSION_LIMIT; i++) {
            for (int j = 0; j < PATCH_VERSION_LIMIT; j++) {
                MinecraftVersion version = new MinecraftVersion(1, i, j);
                versions[(i * MINOR_VERSION_LIMIT) + j] = version;
                System.out.println(version);
            }
        }

        List<MinecraftVersion> sortedVersions = new ArrayList<>(Arrays.asList(versions));
        Collections.shuffle(sortedVersions); // ensure that the versions are out of order to demonstrate compareTo()
        Collections.sort(sortedVersions);

        assertArrayEquals(versions, sortedVersions.toArray(MinecraftVersion[]::new));
    }

}
