package org.minerift.ether.util.nunbt;

import org.minerift.ether.util.nunbt.snbt.Snbt;
import org.minerift.ether.util.nunbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nunbt.tags.Tag;

public interface TagCodec<T extends Tag<?>> {

    // TODO: return bytes written
    void writeTag(NbtTraverser nbt, T tag);

    T readTag(NbtTraverser nbt, String name);

    T readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException;

    // return bytes skipped
    int skip(NbtTraverser nbt);
}
