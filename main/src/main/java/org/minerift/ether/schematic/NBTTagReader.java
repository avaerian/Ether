package org.minerift.ether.schematic;

import org.jnbt.*;
import org.minerift.ether.schematic.types.SpongeSchematic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

// Read schematic NBT tags with helpful utilities
// TODO: cleanup class
public class NBTTagReader {


    // GENERIC UTILITY METHODS

    private static <E extends Tag> Optional<E> getPossibleTag(CompoundTag head, String name) {
        return Optional.ofNullable((E) head.getValue().get(name));
    }

    private static <E extends Tag> E getTagOrThrow(CompoundTag head, String name) {
        return (E) getPossibleTag(head, name).orElseThrow();
    }


    // GET TAG IF PRESENT METHODS

    public static Optional<StringTag> findStringTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<ShortTag> findShortTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<LongTag> findLongTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<LongArrayTag> findLongArrayTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<ListTag> findListTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<IntTag> findIntTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<IntArrayTag> findIntArrayTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<FloatTag> findFloatTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    // TODO: evaluate usefulness of this
    public static Optional<EndTag> findEndTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<DoubleTag> findDoubleTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<CompoundTag> findCompoundTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<List<CompoundTag>> findCompoundListTag(CompoundTag head, String name) {
        return findListTag(head, name).map(listTag -> listTag.getValue().stream().map(tag -> (CompoundTag) tag).toList());
    }

    public static Optional<ByteTag> findByteTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }

    public static Optional<ByteArrayTag> findByteArrayTag(CompoundTag head, String name) {
        return getPossibleTag(head, name);
    }


    // GET TAG OR THROW METHODS
    // GET VALUE OR THROW METHODS

    public static StringTag getStringTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static String getStringOrThrow(CompoundTag head, String name) {
        return getStringTagOrThrow(head, name).getValue();
    }

    public static ShortTag getShortTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static short getShortOrThrow(CompoundTag head, String name) {
        return getShortTagOrThrow(head, name).getValue();
    }

    public static LongTag getLongTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static long getLongOrThrow(CompoundTag head, String name) {
        return getLongTagOrThrow(head, name).getValue();
    }

    public static LongArrayTag getLongArrayTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static long[] getLongArrayOrThrow(CompoundTag head, String name) {
        return getLongArrayTagOrThrow(head, name).getValue();
    }

    public static ListTag getListTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static List<Tag> getListOrThrow(CompoundTag head, String name) {
        return getListTagOrThrow(head, name).getValue();
    }

    public static IntTag getIntTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static int getIntOrThrow(CompoundTag head, String name) {
        return getIntTagOrThrow(head, name).getValue();
    }

    public static IntArrayTag getIntArrayTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static int[] getIntArrayOrThrow(CompoundTag head, String name) {
        return getIntArrayTagOrThrow(head, name).getValue();
    }

    public static FloatTag getFloatTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static float getFloatOrThrow(CompoundTag head, String name) {
        return getFloatTagOrThrow(head, name).getValue();
    }

    // TODO: evaluate usefulness of this
    public static EndTag getEndTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    // TODO: evaluate usefulness of this
    public static Object getEndOrThrow(CompoundTag head, String name) {
        return getEndTagOrThrow(head, name).getValue();
    }

    public static DoubleTag getDoubleTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static double getDoubleOrThrow(CompoundTag head, String name) {
        return getDoubleTagOrThrow(head, name).getValue();
    }

    public static CompoundTag getCompoundTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static Map<String, Tag> getCompoundMapOrThrow(CompoundTag head, String name) {
        return getCompoundTagOrThrow(head, name).getValue();
    }

    public static List<CompoundTag> getCompoundTagListOrThrow(CompoundTag head, String name) {
        return getListOrThrow(head, name).stream().map(tag -> (CompoundTag) tag).toList();
    }

    public static ByteTag getByteTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static byte getByteOrThrow(CompoundTag head, String name) {
        return getByteTagOrThrow(head, name).getValue();
    }

    public static ByteArrayTag getByteArrayTagOrThrow(CompoundTag head, String name) {
        return getTagOrThrow(head, name);
    }

    public static byte[] getByteArrayOrThrow(CompoundTag head, String name) {
        return getByteArrayTagOrThrow(head, name).getValue();
    }

    private NBTTagReader() {}
}
