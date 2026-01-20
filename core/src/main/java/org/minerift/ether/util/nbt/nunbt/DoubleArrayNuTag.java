package org.minerift.ether.util.nbt.nunbt;

import org.minerift.ether.util.nbt.NbtReadException;
import org.minerift.ether.util.nbt.NbtTraverser;
import org.minerift.ether.util.nbt.TagCodec;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.DoubleTag;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.TagType;
import org.minerift.ether.util.nbt.tags.array.ArrayTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import java.util.ArrayList;
import java.util.List;

import static org.minerift.ether.util.nbt.nunbt.DoubleArrayNuTag.Codec.doublesToTags;
import static org.minerift.ether.util.nbt.tags.NbtOptions.USE_NUNBT_IO;
import static org.minerift.ether.util.nbt.tags.TagTypes.DOUBLE;
import static org.minerift.ether.util.nbt.tags.TagTypes.LIST;

public class DoubleArrayNuTag extends ArrayTag<double[]> {

    public DoubleArrayNuTag(String name, double[] value) {
        super(name, value);
    }

    @Override
    public Tag copy(boolean copyArray) {
        return new DoubleArrayNuTag(name, copyArray ? value.clone() : value);
    }

    @Override
    public TagType<DoubleArrayNuTag> type() {
        return NuTagTypes.DOUBLE_ARRAY;
    }

    @Override
    public DoubleArrayNuTag copy() {
        return new DoubleArrayNuTag(name, value);
    }

    public ListTag<DoubleTag> toPrimitive() {
        List<DoubleTag> tags = doublesToTags(value);
        return new ListTag<>(name, DOUBLE, tags);
    }

    public static class Codec implements TagCodec<DoubleArrayNuTag> {

        public static double[] tagsToDoubles(List<DoubleTag> tags) {
            double[] ds = new double[tags.size()];
            int i = 0;
            for(DoubleTag tag : tags) {
                ds[i++] = tag.getAsDouble();
            }
            return ds;
        }

        public static List<DoubleTag> doublesToTags(double[] ds) {
            List<DoubleTag> tags = new ArrayList<>(ds.length);
            for(double d : ds) {
                tags.add(DoubleTag.valueOf(d));
            }
            return tags;
        }

        @Override
        public void writeTag(NbtTraverser nbt, DoubleArrayNuTag tag) {
            if(nbt.supports(USE_NUNBT_IO)) {
                nbt.writeDoubleArray(tag.value);
            } else {
                List<DoubleTag> doubles = doublesToTags(tag.getValue());
                LIST.codec().writeTag(nbt, new ListTag<>(tag.name, DOUBLE, doubles));
            }
        }

        @Override
        public void writeTag(StringBuilder str, DoubleArrayNuTag tag) {
            List<DoubleTag> list = doublesToTags(tag.getValue());
            LIST.codec().writeTag(str, new ListTag<>(tag.name, DOUBLE, list));
        }

        @Override
        public DoubleArrayNuTag readTag(NbtTraverser nbt, String name) throws NbtReadException {
            if(nbt.supports(USE_NUNBT_IO)) {
                int len = nbt.readInt();
                double[] doubles = new double[len];
                nbt.readDoubles(doubles);
                return new DoubleArrayNuTag(name, doubles);
            } else {
                throw new NbtReadException("NuNBT tag found, but reader doesn't support NuNBT types"); // TODO: review
            }
        }

        @Override
        public DoubleArrayNuTag readTag(Snbt.Parser snbt, String name) throws UnexpectedTokenException {
            ListTag<DoubleTag> list = LIST.codec().readTag(snbt, name);
            double[] ds = tagsToDoubles(list.getValue());
            return new DoubleArrayNuTag(name, ds);
        }

        @Override // for reading, at least for now
        public int skip(NbtTraverser nbt) {
            if(nbt.supports(USE_NUNBT_IO)) {
                int len = nbt.readInt();
                nbt.skip(len * Double.BYTES);
                return Integer.BYTES + (len * Double.BYTES);
            } else {
                return LIST.codec().skip(nbt); // TODO: review
            }
        }
    }
}
