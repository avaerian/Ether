package org.minerift.ether.nms;

import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;

@Deprecated
public interface RegistryAccess {

    NamespacedKey getNamespacedKey(ItemStack bukkitItem);
    NamespacedKey getNamespacedKey(BlockState bukkitBlock);
    NamespacedKey getNamespacedKey(BlockData bukkitBlock);
    NamespacedKey getDimNamespacedKey(World world);
    NamespacedKey getNamespacedKey(Biome biome);
}
