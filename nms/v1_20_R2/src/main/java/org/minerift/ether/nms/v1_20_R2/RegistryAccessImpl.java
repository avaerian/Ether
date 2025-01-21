package org.minerift.ether.nms.v1_20_R2;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.minerift.ether.nms.RegistryAccess;

public class RegistryAccessImpl implements RegistryAccess {

    @Override
    public NamespacedKey getNamespacedKey(ItemStack bukkitItem) {
        ResourceLocation resource = BuiltInRegistries.ITEM.getKey(NativeTypeConversions.toNative(bukkitItem).getItem());
        return NativeTypeConversions.fromNative(resource);
    }

    @Override
    public NamespacedKey getNamespacedKey(BlockState bukkitBlock) {
        ResourceLocation resource = BuiltInRegistries.BLOCK.getKey(NativeTypeConversions.toNative(bukkitBlock).getBlock());
        return NativeTypeConversions.fromNative(resource);
    }

    @Override
    public NamespacedKey getNamespacedKey(BlockData bukkitBlock) {
        ResourceLocation resource = BuiltInRegistries.BLOCK.getKey(NativeTypeConversions.toNative(bukkitBlock).getBlock());
        return NativeTypeConversions.fromNative(resource);
    }

    @Override
    public NamespacedKey getDimNamespacedKey(World world) {
        ResourceLocation resource = NativeTypeConversions.toNative(world).dimension().location();
        return NativeTypeConversions.fromNative(resource);
    }

}
