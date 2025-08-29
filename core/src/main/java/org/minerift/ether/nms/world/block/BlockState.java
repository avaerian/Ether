package org.minerift.ether.nms.world.block;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.NativeTypeConversions;
import org.minerift.ether.util.fn.Copy;

public interface BlockState<NBS> /*extends Copy<BlockState<NBS>>*/ {

    static BlockState<?> of(String id) throws BlockStateNotFoundException {
        return Ether.getNms().getConverter().asBlockState(id);
    }

    static BlockState<?> of(String id, @Nullable String fallback) {
        return Ether.getNms().getConverter().asBlockState(id, fallback);
    }


    boolean hasAttribute(Attribute<?> attr);

    // mirror of getProperty()
    <T> T getAttribute(Attribute<T> attr);
    <T> T tryGetAttribute(Attribute<T> attr);

    // mirror of setProperty()
    <T> BlockState<NBS> setAttribute(Attribute<T> attr, T val);
    <T> BlockState<NBS> trySetAttribute(Attribute<T> attr, T val);

    boolean hasBlockEntity();

    boolean canBeReplaced();

    boolean isAir();

    Material getBukkitMaterial();

    NBS asNative();

    NativeTypeConversions<NBS,?,?,?,?> getConverter();
}
