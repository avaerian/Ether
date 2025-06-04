package org.minerift.ether.util.nunbt;

import org.minerift.ether.util.nunbt.tags.EndTag;
import org.minerift.ether.util.nunbt.tags.Tag;
import org.minerift.ether.util.nunbt.tags.TagType;

import java.nio.ByteBuffer;
import java.util.function.Predicate;

public class NbtReader extends NbtTraverser {

    public NbtReader(ByteBuffer buffer, boolean bigEndian) {
        super(buffer, bigEndian);
    }

    public NbtReader(ByteBuffer buffer, boolean bigEndian, Predicate<TagHeader> tagSelector) {
        super(buffer, bigEndian, tagSelector);
    }

    public Tag<?> readNextTag() {
        byte typeId = readByte();
        TagType type = TagType.lookup(typeId);
        if(type == TagType.END) {
            return EndTag.INSTANCE;
        }
        String name = readUTF8();
        return type.readTag(this, name);
    }

}
