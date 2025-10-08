package org.minerift.ether.nms.v1_20_R2;

import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.array.LongArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;
import org.minerift.ether.util.nbt.transmute.MapTags;
import org.minerift.ether.util.nbt.transmute.NbtTransmuteException;

import java.util.Map;

import static net.minecraft.nbt.Tag.*;

public class NativeTagBuilder {

    // Expecting primitive tags
    public static net.minecraft.nbt.Tag from(Tag tag) throws NbtTransmuteException {
        net.minecraft.nbt.Tag ntag = switch (tag.type().getId()) {
            case TAG_END    -> net.minecraft.nbt.EndTag.INSTANCE;

            case TAG_BYTE   -> net.minecraft.nbt.ByteTag.valueOf( ((ByteTag)tag).getAsByte() );
            case TAG_SHORT  -> net.minecraft.nbt.ShortTag.valueOf( ((ShortTag)tag).getAsShort() );
            case TAG_INT    -> net.minecraft.nbt.IntTag.valueOf( ((IntTag)tag).getAsInt() );
            case TAG_LONG   -> net.minecraft.nbt.LongTag.valueOf( ((LongTag)tag).getAsLong() );
            case TAG_FLOAT  -> net.minecraft.nbt.FloatTag.valueOf( ((FloatTag)tag).getAsFloat() );
            case TAG_DOUBLE -> net.minecraft.nbt.DoubleTag.valueOf( ((DoubleTag)tag).getAsDouble() );

            case TAG_STRING -> net.minecraft.nbt.StringTag.valueOf( ((StringTag)tag).getStrVal() );

            case TAG_BYTE_ARRAY -> new net.minecraft.nbt.ByteArrayTag( ((ByteArrayTag)tag).getValue() );
            case TAG_INT_ARRAY  -> new net.minecraft.nbt.IntArrayTag( ((IntArrayTag)tag).getValue() );
            case TAG_LONG_ARRAY -> new net.minecraft.nbt.LongArrayTag( ((LongArrayTag)tag).getValue() );

            case TAG_LIST -> {
                ListTag<?> listTag = (ListTag<?>)tag;
                net.minecraft.nbt.ListTag nativeListTag = new net.minecraft.nbt.ListTag();
                for(Tag child : listTag) {
                    nativeListTag.add(from(child));
                }
                yield nativeListTag;
            }

            case TAG_COMPOUND -> {
                CompoundTag _tag = ((CompoundTag)tag);
                net.minecraft.nbt.CompoundTag compound = new net.minecraft.nbt.CompoundTag();
                for(Map.Entry<String, Tag> entry : _tag) {
                    compound.put(entry.getKey(), from(entry.getValue()));
                }
                yield compound;
            }
            default -> throw new NbtTransmuteException("Unable to adapt " + tag.type() + " to a primitive NBT type");
        };
        return ntag;
    }

    public static net.minecraft.nbt.Tag from(Tag _tag, MapTags mapper) throws NbtTransmuteException {
        Tag tag = mapper.map(_tag);
        return from(tag);
    }

}
