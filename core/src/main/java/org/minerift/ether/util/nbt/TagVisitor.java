package org.minerift.ether.util.nbt;

import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.array.LongArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

// available as a util if needed
@Deprecated
public interface TagVisitor {

    void visitEnd(EndTag tag);

    void visitByte(ByteTag tag);
    void visitShort(ShortTag tag);
    void visitInt(IntTag tag);
    void visitLong(LongTag tag);
    void visitFloat(FloatTag tag);
    void visitDouble(DoubleTag tag);

    void visitString(StringTag tag);

    void visitByteArray(ByteArrayTag tag);
    void visitIntArray(IntArrayTag tag);
    void visitLongArray(LongArrayTag tag);

    <T extends Tag> void visitList(ListTag<T> tag);
    void visitCompound(CompoundTag tag);
}
