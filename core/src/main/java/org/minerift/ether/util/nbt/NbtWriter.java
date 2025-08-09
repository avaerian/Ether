package org.minerift.ether.util.nbt;

import org.minerift.ether.util.nbt.tags.Tag;

import java.nio.ByteBuffer;
import java.util.function.Predicate;

public class NbtWriter extends NbtTraverser {
    public NbtWriter(ByteBuffer buffer, boolean bigEndian) {
        super(buffer, bigEndian);
    }

    public NbtWriter(ByteBuffer buffer, boolean bigEndian, Predicate<TagHeader> tagSelector) {
        super(buffer, bigEndian, tagSelector);
    }

    public NbtWriter() {
        super(ByteBuffer.allocate(4096), true); // FIXME: remove 4096 magic number (refactor as constant)
    }

    public void writeTag(Tag tag) {
        writeByte(tag.getType().getId());
        writeUTF8(tag.getName());
        ((TagCodec<Tag>)tag.getType().codec()).writeTag(this, tag);
    }
}
