package org.minerift.ether.util.nbt.tags;

import org.jetbrains.annotations.Nullable;
import org.minerift.ether.util.UnreachableException;
import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.TagVisitor;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;

import static org.minerift.ether.util.nbt.tags.TagTypes.END;

public class EndTag extends Tag {

    public static final EndTag INST = new EndTag();

    private EndTag() {
        super();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Unable to set name for NBT end tag");
    }

    @Override
    public void accept(TagVisitor visit) {
        visit.visitEnd(this);
    }

    @Override
    public TagType<EndTag> type() {
        return END;
    }

    @Override
    public Tag copy() {
        throw new UnsupportedOperationException("Copying unsupported for end tag");
    }

    public static class Codec implements TagCodec<EndTag> {
        @Override
        public EndTag readTag(NbtTraverser nbt, @Nullable String name) {
            return EndTag.INST;
        }

        @Override
        public EndTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            throw new UnsupportedOperationException("End tag unsupported for SNBT");
        }

        public EndTag readTag(NbtTraverser nbt) {
            return EndTag.INST;
        }

        @Override
        public void writeTag(NbtTraverser nbt, EndTag tag) {
            writeTag(nbt);
        }

        @Override
        public void writeTag(StringBuilder str, EndTag tag) {
            throw new UnreachableException("End tag cannot be written as SNBT");
        }

        public void writeTag(NbtTraverser nbt) {
            nbt.writeByte(END.getId());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            return 1; // TODO: log, review this
        }
    }
}
