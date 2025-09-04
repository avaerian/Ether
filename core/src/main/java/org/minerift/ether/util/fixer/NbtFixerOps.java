package org.minerift.ether.util.fixer;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import org.minerift.ether.util.Note;
import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Note("still needs to be finished")
public class NbtFixerOps implements DynamicOps<Tag> {

    public static final NbtFixerOps INSTANCE = new NbtFixerOps();

    private NbtFixerOps() {}

    @Override
    public Tag empty() {
        return EndTag.INST;
    }

    @Override
    public Tag emptyMap() {
        return new CompoundTag.LazyCompoundTag();
    }

    @Override
    public Tag emptyList() {
        return ListTag.empty("");
    }

    @Override
    public <U> U convertTo(DynamicOps<U> outOps, Tag input) {
        return null; // TODO
    }

    @Override
    public DataResult<Number> getNumberValue(Tag input) {
        if(input instanceof ScalarTag tag) {
            return DataResult.success(tag.getAsNumber());
        }
        return DataResult.error(() -> "Not a scalar tag");
    }

    @Override
    public Number getNumberValue(Tag input, Number defaultValue) {
        return getNumberValue(input).result().orElse(defaultValue);
    }

    @Override
    public Tag createNumeric(Number i) {
        return DoubleTag.valueOf(i.doubleValue()); // TODO: review
    }

    @Override
    public Tag createByte(byte value) {
        return ByteTag.valueOf(value);
    }

    @Override
    public Tag createShort(short value) {
        return ShortTag.valueOf(value);
    }

    @Override
    public Tag createInt(int value) {
        return IntTag.valueOf(value);
    }

    @Override
    public Tag createLong(long value) {
        return LongTag.valueOf(value);
    }

    @Override
    public Tag createFloat(float value) {
        return FloatTag.valueOf(value);
    }

    @Override
    public Tag createDouble(double value) {
        return DoubleTag.valueOf(value);
    }

    @Override
    public DataResult<Boolean> getBooleanValue(Tag input) {
        if(input instanceof ByteTag tag) {
            return DataResult.success(tag.getAsBoolean());
        }
        return DataResult.error(() -> "Not a byte tag (for bool)");
    }

    @Override
    public Tag createBoolean(boolean value) {
        return ByteTag.valueOf((byte)(value ? 1 : 0));
    }

    @Override
    public DataResult<String> getStringValue(Tag input) {
        if(input instanceof StringTag tag) {
            return DataResult.success(tag.getStrVal());
        }
        return DataResult.error(() -> "Not a string tag");
    }

    @Override
    public Tag createString(String value) {
        return StringTag.valueOf(value);
    }

    @Override
    public DataResult<Tag> mergeToList(Tag list, Tag value) {
        return null;
    }

    @Override
    public DataResult<Tag> mergeToMap(Tag map, Tag key, Tag value) {
        return null;
    }

    @Override
    public DataResult<Stream<Pair<Tag, Tag>>> getMapValues(Tag input) {
        return null;
    }

    @Override
    public DataResult<Consumer<BiConsumer<Tag, Tag>>> getMapEntries(Tag input) {
        return DynamicOps.super.getMapEntries(input);
    }

    @Override
    public Tag createMap(Stream<Pair<Tag, Tag>> map) {
        return null;
    }

    @Override
    public DataResult<MapLike<Tag>> getMap(Tag input) {
        return DynamicOps.super.getMap(input);
    }

    @Override
    public Tag createMap(Map<Tag, Tag> map) {
        return DynamicOps.super.createMap(map);
    }

    @Override
    public DataResult<Stream<Tag>> getStream(Tag input) {
        return null;
    }

    @Override
    public DataResult<Consumer<Consumer<Tag>>> getList(Tag input) {
        return DynamicOps.super.getList(input);
    }

    @Override
    public Tag createList(Stream<Tag> input) {
        ListTag tag = input.collect(Collector.of(
                () -> ListTag.empty(""),
                (l, t) -> l.addTagOrThrow(t),
                (l1, l2) -> {
                    // TODO: no verification done here; review?
                    List<Tag> children = new ArrayList<>(l1.size() + l2.size());
                    children.addAll(l1.getValue());
                    children.addAll(l2.getValue());
                    return new ListTag("", children);
                }
        ));
        return tag;
    }

    @Override
    public Tag createIntList(IntStream input) {
        return IntArrayTag.valueOf(input.toArray());
    }

    @Override
    public Tag remove(Tag input, String key) {
        return null;
    }
}
