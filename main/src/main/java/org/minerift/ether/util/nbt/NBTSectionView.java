package org.minerift.ether.util.nbt;

import org.minerift.ether.util.nbt.tags.*;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class NBTSectionView {

    private final CompoundTag head;

    public NBTSectionView(CompoundTag head) {
        this.head = head;
    }

    public String getName() {
        return head.getName();
    }

    private <T extends Tag, R> Optional<R> readValue(String name, Class<T> tagType, Function<T, R> tagToValue) {
        final Tag tag = head.getValue().get(name);
        if(tag == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(tagToValue.apply(tagType.cast(tag)));
    }

    public Optional<Integer> getInt(String name) {
        return readValue(name, IntTag.class, IntTag::getValue);
    }

    public Optional<Short> getShort(String name) {
        return readValue(name, ShortTag.class, ShortTag::getValue);
    }

    public Optional<String> getString(String name) {
        return readValue(name, StringTag.class, StringTag::getValue);
    }

    public Optional<Long> getLong(String name) {
        return readValue(name, LongTag.class, LongTag::getValue);
    }

    public Optional<long[]> getLongArray(String name) {
        return readValue(name, LongArrayTag.class, LongArrayTag::getValue);
    }

    public Optional<int[]> getIntArray(String name) {
        return readValue(name, IntArrayTag.class, IntArrayTag::getValue);
    }

    public Optional<Float> getFloat(String name) {
        return readValue(name, FloatTag.class, FloatTag::getValue);
    }

    public <T> Optional<T[]> readArray(String name, Class<T> valueType, Function<Tag, T> tagToValue) {
        AtomicReference<Optional<T[]>> optional = new AtomicReference<>(Optional.empty());
        getList(name).ifPresent((tagList) -> {

            T[] elements = (T[]) Array.newInstance(valueType, tagList.size());
            for(int i = 0; i < tagList.size(); i++) {
                elements[i] = tagToValue.apply(tagList.get(i));
            }

            optional.set(Optional.of(elements));
        });
        return optional.get();
    }

    public Optional<Float[]> getFloatArray(String name) {
        return readArray(name, Float.class, (tag) -> ((FloatTag)tag).getValue());
    }

    public Optional<Double[]> getDoubleArray(String name) {
        return readArray(name, Double.class, (tag) -> ((DoubleTag)tag).getValue());
    }

    public Optional<Byte> getByte(String name) {
        return readValue(name, ByteTag.class, ByteTag::getValue);
    }

    public Optional<byte[]> getByteArray(String name) {
        return readValue(name, ByteArrayTag.class, ByteArrayTag::getValue);
    }

    public Optional<List<Tag>> getList(String name) {
        return readValue(name, ListTag.class, ListTag::getValue);
    }

    public Optional<NBTSectionView> getSectionView(String name) {
        return readValue(name, CompoundTag.class, NBTSectionView::new);
    }

    public Map<String, Tag> getSectionTags() {
        return head.getValue();
    }

}
