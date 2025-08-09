package org.minerift.ether.nms.world;

import org.bukkit.NamespacedKey;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.NativeTypeConversions;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

public interface ItemStack<NIS> {

    static ItemStack<?> of(String snbt) {
        return Ether.getNms().getConverter().asItemStack(snbt);
    }

    static ItemStack<?> of(CompoundTag nbt) {
        return Ether.getNms().getConverter().asItemStack(nbt);
    }

    static ItemStack<?> of(org.bukkit.inventory.ItemStack bukkitItem) {
        return Ether.getNms().getConverter().asItemStack(bukkitItem);
    }

    CompoundTag getNbtTag();
    String getAsString();

    String getResourceLocation();
    NamespacedKey getNamespacedKey();

    NIS asNative();

    NativeTypeConversions<?,?,?,?,NIS> getConverter();

}
