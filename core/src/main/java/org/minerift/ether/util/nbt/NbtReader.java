package org.minerift.ether.util.nbt;

import org.minerift.ether.util.nbt.tags.EndTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.TagType;

import java.nio.ByteBuffer;
import java.util.function.Predicate;

import static org.minerift.ether.util.nbt.tags.TagTypes.END;
import static org.minerift.ether.util.nbt.tags.TagTypes.lookup;

public class NbtReader extends NbtTraverser {

    public NbtReader(ByteBuffer buffer, boolean bigEndian) {
        super(buffer, bigEndian);
    }

    public NbtReader(ByteBuffer buffer, boolean bigEndian, Predicate<TagHeader> tagSelector) {
        super(buffer, bigEndian, tagSelector);
    }

    public Tag readNextTag() throws NbtReadException {
        byte typeId = readByte();
        TagType<?> type = lookup(typeId);
        if(type == END) {
            return EndTag.INST;
        }
        String name = readUTF8();
        return type.codec().readTag(this, name);
    }

}
