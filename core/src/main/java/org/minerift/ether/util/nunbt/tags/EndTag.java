package org.minerift.ether.util.nunbt.tags;

import org.jetbrains.annotations.Nullable;
import org.minerift.ether.util.nunbt.NbtTraverser;
import org.minerift.ether.util.nunbt.TagCodec;
import org.minerift.ether.util.nunbt.snbt.Snbt;
import org.minerift.ether.util.nunbt.snbt.UnexpectedTokenException;

public class EndTag extends Tag<Void> {

    public static final EndTag INSTANCE = new EndTag();

    private EndTag(String name) {
        super();
    }

    private EndTag() {
        super();
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("Unable to set name for NBT end tag!");
    }

    @Override
    public TagType getType() {
        return TagType.END;
    }

    @Override
    public Void getValue() {
        throw new UnsupportedOperationException("Unable to get value for NBT end tag!");
    }

    @Override
    public void setValue(Void value) {
        throw new UnsupportedOperationException("Unable to set value for NBT end tag!");
    }

    @Override
    public Tag<Void> copy() {
        throw new UnsupportedOperationException("Copying unsupported for end tag");
    }

    public static class Codec implements TagCodec<EndTag> {
        @Override
        public EndTag readTag(NbtTraverser nbt, @Nullable String name) {
            return EndTag.INSTANCE;
        }

        @Override
        public EndTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            throw new UnsupportedOperationException("End tag unsupported for SNBT");
        }

        public EndTag readTag(NbtTraverser nbt) {
            return EndTag.INSTANCE;
        }

        @Override
        public void writeTag(NbtTraverser nbt, EndTag tag) {
            writeTag(nbt);
        }

        public void writeTag(NbtTraverser nbt) {
            nbt.writeByte(TagType.END.getId());
        }

        @Override
        public int skip(NbtTraverser nbt) {
            return 0;
        }
    }
}
