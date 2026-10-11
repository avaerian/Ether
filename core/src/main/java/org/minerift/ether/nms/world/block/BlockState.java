package org.minerift.ether.nms.world.block;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;
import org.minerift.ether.Ether;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.NativeTypeConversions;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

public interface BlockState<NBS> /*extends Copy<BlockState<NBS>>*/ {

    static BlockState<?> of(String id) throws BlockStateNotFoundException {
        return Ether.inst().getNms().getConverter().asBlockState(id);
    }

    // Nullable to allow throwing exception at runtime without explicit exception handling
    static BlockState<?> of(String id, @Nullable String fallback) {
        return Ether.inst().getNms().getConverter().asBlockState(id, fallback);
    }

    static BlockState<?> of(NamespacedKey key) throws BlockStateNotFoundException {
        return of(key.asString());
    }

    static BlockState<?> of(NamespacedKey key, NamespacedKey fallback) {
        return of(key.asString(), fallback.asString());
    }

    static BlockState<?> of(Block block) {
        return Ether.inst().getNms().getConverter().asBlockState(block);
    }

    static BlockState<?> of(org.bukkit.block.BlockState bukkitState) {
        return Ether.inst().getNms().getConverter().asBlockState(bukkitState);
    }

    String getResourceLocation();

    boolean hasAttribute(Attribute<?> attr);

    // mirror of getProperty()
    <T> T getAttribute(Attribute<T> attr);
    <T> T tryGetAttribute(Attribute<T> attr);

    // mirror of setProperty()
    <T> BlockState<NBS> setAttribute(Attribute<T> attr, T val);
    <T> BlockState<NBS> trySetAttribute(Attribute<T> attr, T val);

    boolean is(String id);
    boolean is(BlockState<NBS> state);

    boolean hasBlockEntity();
    boolean canBeReplaced();
    boolean isAir();

    @Range(from = 0, to = 15) int getLightEmission();

    @Deprecated Material getBukkitMaterial();

    NBS asNative();

    NativeTypeConversions<NBS,?,?,?,?> getConverter();

    CompoundTag propsAsNbt();
    @Experimental String getAsString();
}

