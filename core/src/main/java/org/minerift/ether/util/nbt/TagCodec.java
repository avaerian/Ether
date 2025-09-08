package org.minerift.ether.util.nbt;

import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.Tag;

public interface TagCodec<T extends Tag> {

    // TODO: return bytes written
    void writeTag(NbtTraverser nbt, T tag);

    T readTag(NbtTraverser nbt, String name) throws NbtReadException;

    T readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException;

    // return bytes skipped
    int skip(NbtTraverser nbt);
}
