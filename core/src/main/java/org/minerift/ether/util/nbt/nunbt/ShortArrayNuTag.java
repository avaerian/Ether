package org.minerift.ether.util.nbt.nunbt;

import org.minerift.ether.util.nbt.NbtReadException;
import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.TagVisitor;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.ShortTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.TagType;
import org.minerift.ether.util.nbt.tags.array.ArrayTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import java.util.ArrayList;
import java.util.List;

import static org.minerift.ether.util.nbt.nunbt.NuTagTypes.SHORT_ARRAY;
import static org.minerift.ether.util.nbt.nunbt.ShortArrayNuTag.Codec.shortsToTags;
import static org.minerift.ether.util.nbt.tags.NbtOptions.USE_NUNBT_IO;
import static org.minerift.ether.util.nbt.tags.TagTypes.LIST;
import static org.minerift.ether.util.nbt.tags.TagTypes.SHORT;

public class ShortArrayNuTag extends ArrayTag<short[]> {

    public ShortArrayNuTag(String name, short[] value) {
        super(name, value);
    }

    @Override
    public void accept(TagVisitor visit) {
        // TODO
    }

    @Override
    public TagType<ShortArrayNuTag> type() {
        return SHORT_ARRAY;
    }

    @Override
    public ShortArrayNuTag copy() {
        return new ShortArrayNuTag(name, value);
    }

    @Override
    public ShortArrayNuTag copy(boolean copyArray) {
        return  new ShortArrayNuTag(name, copyArray ? value.clone() : value);
    }

    public ListTag<ShortTag> toPrimitive() {
        List<ShortTag> tags = shortsToTags(value);
        return new ListTag<>(name, SHORT, tags);
    }

    public static class Codec implements TagCodec<ShortArrayNuTag> {

        public static short[] tagsToShorts(List<ShortTag> tags) {
            short[] ss = new short[tags.size()];
            int i = 0;
            for(ShortTag tag : tags) {
                ss[i++] = tag.getAsShort();
            }
            return ss;
        }

        public static List<ShortTag> shortsToTags(short[] ss) {
            List<ShortTag> list = new ArrayList<>(ss.length);
            for(short s : ss) {
                list.add(ShortTag.valueOf(s));
            }
            return list;
        }

        @Override
        public void writeTag(NbtTraverser nbt, ShortArrayNuTag tag) {
            if(nbt.supports(USE_NUNBT_IO)) {
                nbt.writeInt(tag.value.length);
                nbt.writeShortArray(tag.value);
            } else {
                List<ShortTag> tags = shortsToTags(tag.getValue());
                LIST.codec().writeTag(nbt, new ListTag<>(tag.getName(), SHORT, tags));
            }
        }

        @Override
        public void writeTag(StringBuilder str, ShortArrayNuTag tag) {

        }

        @Override
        public ShortArrayNuTag readTag(NbtTraverser nbt, String name) throws NbtReadException {
            if(nbt.supports(USE_NUNBT_IO)) {
                int len = nbt.readInt();
                short[] ss = new short[len];
                nbt.readShorts(ss);
                return new ShortArrayNuTag(name, ss);
            } else {
                ListTag<?> list = LIST.codec().readTag(nbt, name);
                if(!list.childTypeIs(SHORT)) {
                    throw new NbtReadException("NuNbtIO unsupported, found ListTag with non-ShortTag child type");
                }
                List<ShortTag> tags = list.withChildType(SHORT).getValue();
                return new ShortArrayNuTag(name, tagsToShorts(tags));
            }
        }

        @Override
        public ShortArrayNuTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            return null;
        }

        @Override
        public int skip(NbtTraverser nbt) {
            if(nbt.supports(USE_NUNBT_IO)) {
                int len = nbt.readInt();
                nbt.skip(len * Short.BYTES);
                return Integer.BYTES + (len * Short.BYTES);
            } else {
                return LIST.codec().skip(nbt); // TODO: review
            }
        }
    }
}
