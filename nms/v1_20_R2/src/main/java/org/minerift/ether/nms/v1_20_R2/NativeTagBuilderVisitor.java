package org.minerift.ether.nms.v1_20_R2;

import org.minerift.ether.util.nbt.TagVisitor;
import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.array.LongArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;

import java.util.Map;

public class NativeTagBuilderVisitor implements TagVisitor {

    private net.minecraft.nbt.Tag result;

    public NativeTagBuilderVisitor() {
        this.result = null;
    }

    public net.minecraft.nbt.Tag visit(Tag tag) {
        tag.accept(this);
        return result;
    }

    @Override
    public void visitEnd(EndTag tag) {
        result = net.minecraft.nbt.EndTag.INSTANCE;
    }

    @Override
    public void visitByte(ByteTag tag) {
        result = net.minecraft.nbt.ByteTag.valueOf(tag.getAsByte());
    }

    @Override
    public void visitShort(ShortTag tag) {
        result = net.minecraft.nbt.ShortTag.valueOf(tag.getAsShort());
    }

    @Override
    public void visitInt(IntTag tag) {
        result = net.minecraft.nbt.IntTag.valueOf(tag.getAsInt());
    }

    @Override
    public void visitLong(LongTag tag) {
        result = net.minecraft.nbt.LongTag.valueOf(tag.getAsLong());
    }

    @Override
    public void visitFloat(FloatTag tag) {
        result = net.minecraft.nbt.FloatTag.valueOf(tag.getAsFloat());
    }

    @Override
    public void visitDouble(DoubleTag tag) {
        result = net.minecraft.nbt.DoubleTag.valueOf(tag.getAsDouble());
    }

    @Override
    public void visitString(StringTag tag) {
        result = net.minecraft.nbt.StringTag.valueOf(tag.getStrVal());
    }

    @Override
    public void visitByteArray(ByteArrayTag tag) {
        result = new net.minecraft.nbt.ByteArrayTag(tag.getValue());
    }

    @Override
    public void visitIntArray(IntArrayTag tag) {
        result = new net.minecraft.nbt.IntArrayTag(tag.getValue());
    }

    @Override
    public void visitLongArray(LongArrayTag tag) {
        result = new net.minecraft.nbt.LongArrayTag(tag.getValue());
    }

    @Override
    public void visitList(ListTag<?> tag) {
        net.minecraft.nbt.ListTag nt = new net.minecraft.nbt.ListTag();
        for(Tag c : tag) {
            nt.add(new NativeTagBuilderVisitor().visit(c));
        }
        result = nt;
    }

    @Override
    public void visitCompound(CompoundTag tag) {
        net.minecraft.nbt.CompoundTag nt = new net.minecraft.nbt.CompoundTag();
        for(Map.Entry<String, Tag> e : tag) {
            nt.put(e.getKey(), new NativeTagBuilderVisitor().visit(e.getValue()));
        }
        result = nt;
    }
}
