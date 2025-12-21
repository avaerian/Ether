package org.minerift.ether.util.nbt.nunbt;

import org.minerift.ether.util.nbt.NbtReadException;
import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.TagVisitor;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.FloatTag;
import org.minerift.ether.util.nbt.tags.TagType;
import org.minerift.ether.util.nbt.tags.array.ArrayTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import java.util.ArrayList;
import java.util.List;

import static org.minerift.ether.util.nbt.nunbt.FloatArrayNuTag.Codec.floatsToTags;
import static org.minerift.ether.util.nbt.tags.NbtOptions.USE_NUNBT_IO;
import static org.minerift.ether.util.nbt.tags.TagTypes.FLOAT;
import static org.minerift.ether.util.nbt.tags.TagTypes.LIST;

public class FloatArrayNuTag extends ArrayTag<float[]> {

    public FloatArrayNuTag(String name, float[] value) {
        super(name, value);
    }

    @Override
    public void accept(TagVisitor visit) {
        // FIXME
    }

    @Override
    public TagType<FloatArrayNuTag> type() {
        return NuTagTypes.FLOAT_ARRAY;
    }

    @Override
    public FloatArrayNuTag copy() {
        return new FloatArrayNuTag(name, value);
    }

    @Override
    public FloatArrayNuTag copy(boolean copyArray) {
        return new FloatArrayNuTag(name, copyArray ? value.clone() : value);
    }

    public ListTag<FloatTag> toPrimitive() {
        List<FloatTag> tags = floatsToTags(value);
        return new ListTag<>(name, FLOAT, tags);
    }

    public static class Codec implements TagCodec<FloatArrayNuTag> {

        public static List<FloatTag> floatsToTags(float[] fs) {
            List<FloatTag> tags = new ArrayList<>();
            for(float f : fs) {
                tags.add(FloatTag.valueOf(f));
            }
            return tags;
        }

        public static float[] tagsToFloats(List<FloatTag> tags) {
            float[] fs = new float[tags.size()];
            int i = 0;
            for(FloatTag tag : tags) {
                fs[i++] = tag.getAsFloat();
            }
            return fs;
        }

        @Override
        public void writeTag(NbtTraverser nbt, FloatArrayNuTag tag) {
            if(nbt.supports(USE_NUNBT_IO)) {
                nbt.writeInt(tag.getValue().length);
                nbt.writeFloatArray(tag.getValue());
            } else {
                LIST.codec().writeTag(nbt, new ListTag<>( tag.getName(), FLOAT, floatsToTags(tag.getValue()) ) );
            }
        }

        @Override
        public void writeTag(StringBuilder str, FloatArrayNuTag tag) {
            // FIXME: update for NuNbtIO
            LIST.codec().writeTag(str, new ListTag<>( tag.getName(), FLOAT, floatsToTags(tag.getValue()) ) );
        }

        @Override
        public FloatArrayNuTag readTag(NbtTraverser nbt, String name) throws NbtReadException {
            if(nbt.supports(USE_NUNBT_IO)) {
                int len = nbt.readInt();
                float[] fs = new float[len];
                nbt.readFloats(fs);
                return new FloatArrayNuTag(name, fs);
            } else {
                ListTag<FloatTag> list = LIST.codec().readTag(nbt, name); // TODO: review
                return new FloatArrayNuTag(name, tagsToFloats(list.getValue()));
            }
        }

        @Override
        public FloatArrayNuTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            ListTag<FloatTag> list = LIST.codec().readTag(snbt, name);
            return new FloatArrayNuTag(name, tagsToFloats(list.getValue()));
        }

        @Override
        public int skip(NbtTraverser nbt) {
            if(nbt.supports(USE_NUNBT_IO)) {
                int len = nbt.readInt();
                nbt.skip(Float.BYTES * len);
                return Integer.BYTES + (Float.BYTES * len);
            } else {
                return LIST.codec().skip(nbt);
            }
        }
    }
}
