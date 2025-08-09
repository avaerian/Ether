package org.minerift.ether.util.nbt;

import org.minerift.ether.util.nbt.tags.EndTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.PrimitiveTagType;

import java.nio.ByteBuffer;
import java.util.function.Predicate;

public class NbtReader extends NbtTraverser {

    public NbtReader(ByteBuffer buffer, boolean bigEndian) {
        super(buffer, bigEndian);
    }

    public NbtReader(ByteBuffer buffer, boolean bigEndian, Predicate<TagHeader> tagSelector) {
        super(buffer, bigEndian, tagSelector);
    }

    public Tag readNextTag() {
        byte typeId = readByte();
        PrimitiveTagType type = PrimitiveTagType.lookup(typeId);
        if(type == PrimitiveTagType.END) {
            return EndTag.INSTANCE;
        }
        String name = readUTF8();
        return type.readTag(this, name);
    }

}
