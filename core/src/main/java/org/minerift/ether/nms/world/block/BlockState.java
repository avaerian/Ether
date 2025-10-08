package org.minerift.ether.nms.world.block;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.Ether;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.NativeTypeConversions;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

public interface BlockState<NBS> /*extends Copy<BlockState<NBS>>*/ {

    static BlockState<?> of(String id) throws BlockStateNotFoundException {
        return Ether.getNms().getConverter().asBlockState(id);
    }

    // Nullable to allow throwing exception at runtime without explicit exception handling
    static BlockState<?> of(String id, @Nullable String fallback) {
        return Ether.getNms().getConverter().asBlockState(id, fallback);
    }

    static BlockState<?> of(NamespacedKey key) throws BlockStateNotFoundException {
        return of(key.asString());
    }

    static BlockState<?> of(NamespacedKey key, NamespacedKey fallback) {
        return of(key.asString(), fallback.asString());
    }


    boolean hasAttribute(Attribute<?> attr);

    // mirror of getProperty()
    <T> T getAttribute(Attribute<T> attr);
    <T> T tryGetAttribute(Attribute<T> attr);

    // mirror of setProperty()
    <T> BlockState<NBS> setAttribute(Attribute<T> attr, T val);
    <T> BlockState<NBS> trySetAttribute(Attribute<T> attr, T val);

    CompoundTag propsAsNbt();
    @Experimental String getAsString();

    boolean hasBlockEntity();

    boolean canBeReplaced();
    boolean isAir();

    Material getBukkitMaterial();

    NBS asNative();

    NativeTypeConversions<NBS,?,?,?,?> getConverter();
}
