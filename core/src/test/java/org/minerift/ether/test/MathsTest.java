package org.minerift.ether.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec2i;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MathsTest {

    private static Stream<Vec2i> packUnpackTest() {
        return Stream.of(
                new Vec2i(420, 69),
                new Vec2i(42069, 1738),
                new Vec2i(1, 0),
                new Vec2i(0, 1)
        );
    }

    @ParameterizedTest
    @MethodSource
    public void packUnpackTest(Vec2i vec) {
        long xz = Maths.pack(vec, Maths.PackingOrder.XZ);
        long zx = Maths.pack(vec, Maths.PackingOrder.ZX);
        Vec2i unpackedXZ = Maths.unpack(xz, Maths.PackingOrder.XZ);
        Vec2i unpackedZX = Maths.unpack(zx, Maths.PackingOrder.ZX);
        assertEquals(vec, unpackedXZ, "Failed to pack and unpack in XZ order!");
        assertEquals(vec, unpackedZX, "Failed to pack and unpack in ZX order!");
    }

}
