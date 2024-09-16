package org.minerift.ether.nms.v1_19_R3;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.minerift.ether.nms.RegistryAccess;

import static org.minerift.ether.nms.v1_19_R3.NativeTypeConversions.fromNative;
import static org.minerift.ether.nms.v1_19_R3.NativeTypeConversions.toNative;

public class RegistryAccessImpl implements RegistryAccess {

    @Override
    public NamespacedKey getNamespacedKey(ItemStack bukkitItem) {
        ResourceLocation resource = BuiltInRegistries.ITEM.getKey(toNative(bukkitItem).getItem());
        return fromNative(resource);
    }

    @Override
    public NamespacedKey getNamespacedKey(BlockState bukkitBlock) {
        ResourceLocation resource = BuiltInRegistries.BLOCK.getKey(toNative(bukkitBlock).getBlock());
        return fromNative(resource);
    }

    @Override
    public NamespacedKey getNamespacedKey(BlockData bukkitBlock) {
        ResourceLocation resource = BuiltInRegistries.BLOCK.getKey(toNative(bukkitBlock).getBlock());
        return fromNative(resource);
    }

    @Override
    public NamespacedKey getDimNamespacedKey(World world) {
        ResourceLocation resource = toNative(world).dimension().location();
        return fromNative(resource);
    }

}
