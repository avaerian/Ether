package org.minerift.ether.nms.world;

import org.bukkit.Material;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.NativeTypeConversions;

public interface BlockState<NBS> {

    static BlockState<?> of(String id) throws BlockStateNotFoundException {
        return Ether.getNms().getConverter().asBlockState(id);
    }

    static BlockState<?> of(String id, @Nullable String fallback) {
        return Ether.getNms().getConverter().asBlockState(id, fallback);
    }

    boolean hasBlockEntity();

    boolean canBeReplaced();

    boolean isAir();

    Material getBukkitMaterial();

    NBS asNative();

    NativeTypeConversions<NBS, ?, ?, ?> getConverter();
}
