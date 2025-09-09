package org.minerift.ether.nms.v1_20_R2;

import net.minecraft.nbt.TagVisitor;
import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.array.LongArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CoreTagBuilderVisitor implements TagVisitor {

    private Tag result;

    public CoreTagBuilderVisitor() {
        this.result = null;
    }

    public Tag visit(net.minecraft.nbt.Tag nativeTag) {
        nativeTag.accept(this);
        return result;
    }

    @Override
    public void visitString(net.minecraft.nbt.StringTag element) {
        result = new StringTag("", element.getAsString());
    }

    @Override
    public void visitByte(net.minecraft.nbt.ByteTag element) {
        result = new ByteTag("", element.getAsByte());
    }

    @Override
    public void visitShort(net.minecraft.nbt.ShortTag element) {
        result = new ShortTag("", element.getAsShort());
    }

    @Override
    public void visitInt(net.minecraft.nbt.IntTag element) {
        result = new IntTag("", element.getAsInt());
    }

    @Override
    public void visitLong(net.minecraft.nbt.LongTag element) {
        result = new LongTag("", element.getAsLong());
    }

    @Override
    public void visitFloat(net.minecraft.nbt.FloatTag element) {
        result = new FloatTag("", element.getAsFloat());
    }

    @Override
    public void visitDouble(net.minecraft.nbt.DoubleTag element) {
        result = new DoubleTag("", element.getAsDouble());
    }

    @Override
    public void visitByteArray(net.minecraft.nbt.ByteArrayTag element) {
        result = new ByteArrayTag("", element.getAsByteArray());
    }

    @Override
    public void visitIntArray(net.minecraft.nbt.IntArrayTag element) {
        result = new IntArrayTag("", element.getAsIntArray());
    }

    @Override
    public void visitLongArray(net.minecraft.nbt.LongArrayTag element) {
        result = new LongArrayTag("", element.getAsLongArray());
    }

    @Override
    public void visitList(net.minecraft.nbt.ListTag element) {
        TagType childType = TagTypes.lookup(element.getElementType());
        ListTag tag = new ListTag<>("", childType, new ArrayList<>(element.size()));
        for(net.minecraft.nbt.Tag value : element) {
            Tag childTag = new CoreTagBuilderVisitor().visit(value);
            tag.addTag(childTag);
        }
        result = tag;
    }

    @Override
    public void visitCompound(net.minecraft.nbt.CompoundTag compound) {
        CompoundTag tag = new CompoundTag("", new HashMap<>(compound.tags.size()));
        for(Map.Entry<String, net.minecraft.nbt.Tag> entry : compound.tags.entrySet()) {
            Tag childTag = new CoreTagBuilderVisitor().visit(entry.getValue());
            childTag.setName(entry.getKey());
            tag.addTag(childTag);
        }
        result = tag;
    }

    @Override
    public void visitEnd(net.minecraft.nbt.EndTag element) {
        // TODO: log + review
        result = EndTag.INST;
    }
}
