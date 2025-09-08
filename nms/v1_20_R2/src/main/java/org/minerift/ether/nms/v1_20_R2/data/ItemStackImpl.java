package org.minerift.ether.nms.v1_20_R2.data;

import com.google.common.base.Preconditions;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.NamespacedKey;
import org.minerift.ether.nms.v1_20_R2.NativeTypeConversionsImpl;
import org.minerift.ether.nms.world.ItemStack;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import static org.minerift.ether.util.nbt.tags.TagTypes.COMPOUND;

public class ItemStackImpl implements ItemStack<net.minecraft.world.item.ItemStack> {

    private final net.minecraft.world.item.ItemStack item;

    public ItemStackImpl(String snbt) {
        Tag nbt;
        try {
            nbt = Snbt.readTag(snbt);
        } catch (UnexpectedTokenException ex) {
            throw new RuntimeException(ex); // TODO: better exception handling/logging
        }
        Preconditions.checkArgument(nbt.is(COMPOUND));

        net.minecraft.nbt.CompoundTag nativeTag = (net.minecraft.nbt.CompoundTag) NativeTypeConversionsImpl.inst().asNativeTag(nbt);
        this.item = net.minecraft.world.item.ItemStack.of(nativeTag);
    }

    public ItemStackImpl(CompoundTag nbt) {
        net.minecraft.nbt.CompoundTag nativeTag = (net.minecraft.nbt.CompoundTag) NativeTypeConversionsImpl.inst().asNativeTag(nbt);
        this.item = net.minecraft.world.item.ItemStack.of(nativeTag);
    }

    public ItemStackImpl(net.minecraft.world.item.ItemStack item) {
        this.item = item;
    }

    @Override
    public CompoundTag getNbtTag() {
        return (CompoundTag) getConverter().asTag(item.getOrCreateTag());
    }

    @Override
    public String getAsString() {
        return item.getOrCreateTag().getAsString();
    }

    public ResourceLocation getResourceLocationNative() {
        return item.getItemHolder().unwrapKey().orElseThrow().location();
    }

    @Override
    public String getResourceLocation() {
        ResourceLocation loc = getResourceLocationNative();
        return loc.toString();
    }

    @Override
    public NamespacedKey getNamespacedKey() {
        ResourceLocation loc = getResourceLocationNative();
        return new NamespacedKey(loc.getNamespace(), loc.getPath());
    }

    @Override
    public net.minecraft.world.item.ItemStack asNative() {
        return item;
    }

    @Override
    public NativeTypeConversionsImpl getConverter() {
        return NativeTypeConversionsImpl.inst();
    }
}
