package org.minerift.ether.test;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3d;
import org.minerift.ether.math.Vec3i;

import java.io.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(vec, unpackedXZ, "Failed to pack and unpack in XZ order");
        assertEquals(vec, unpackedZX, "Failed to pack and unpack in ZX order");
    }



    private static Stream<Vec2i> serializationVec2iTest() {
        return Stream.of(
                new Vec2i(420, 69),
                new Vec2i(42069, -1738),
                new Vec2i.Mutable(1, 0),
                new Vec2i.Mutable(0, 1)
        );
    }

    private static Stream<Vec3i> serializationVec3iTest() {
        return Stream.of(
                new Vec3i(420, 69, 1738),
                new Vec3i(42069, -1738, 80085),
                new Vec3i.Mutable(1, 0, 5),
                new Vec3i.Mutable(0, -5, 1)
        );
    }

    private static Stream<Vec3d> serializationVec3dTest() {
        return Stream.of(
                new Vec3d(7, 8, 9),
                new Vec3d(Math.PI, Math.TAU, Math.E),
                new Vec3d.Mutable(Math.TAU, Math.E, Math.PI)
        );
    }

    private static void serializeTest(Object original) {
        try {
            var outBytes = new ByteArrayOutputStream();
            var out = new ObjectOutputStream(outBytes);
            out.writeObject(original);

            var inBytes = new ByteArrayInputStream(outBytes.toByteArray());
            var in = new ObjectInputStream(inBytes);
            Object obj = in.readObject();

            assertEquals(original, obj);
        } catch (IOException | ClassNotFoundException ex) {
            fail("Unexpected exception", ex);
        }
    }

    @ParameterizedTest
    @MethodSource
    public void serializationVec2iTest(Vec2i vec) {
        serializeTest(vec);
    }

    @ParameterizedTest
    @MethodSource
    public void serializationVec3iTest(Vec3i vec) {
        serializeTest(vec);
    }

    @ParameterizedTest
    @MethodSource
    public void serializationVec3dTest(Vec3d vec) {
        serializeTest(vec);
    }

}
