package org.minerift.ether.nms.v1_20_R2;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.minerift.ether.nms.RegistryAccess;
import org.minerift.ether.util.reflect.Reflect;

// TODO: refactor to get resouce location for items, biomes, dims, etc. for converting to NamespacedKey's
public class RegistryAccessImpl implements RegistryAccess {

    @Override
    public NamespacedKey getNamespacedKey(ItemStack bukkitItem) {
        ResourceLocation resource = BuiltInRegistries.ITEM.getKey(getConverter().asNativeItem(bukkitItem).getItem());
        return getConverter().asNamespacedKey(resource);
    }

    @Override
    public NamespacedKey getNamespacedKey(BlockState bukkitBlock) {
        ResourceLocation resource = BuiltInRegistries.BLOCK.getKey(getConverter().asNativeBlockState(bukkitBlock).getBlock());
        return getConverter().asNamespacedKey(resource);
    }

    @Override
    public NamespacedKey getNamespacedKey(BlockData bukkitBlock) {
        ResourceLocation resource = BuiltInRegistries.BLOCK.getKey(getConverter().asNativeBlockState(bukkitBlock).getBlock());
        return getConverter().asNamespacedKey(resource);
    }

    @Override
    public NamespacedKey getDimNamespacedKey(World world) {
        ResourceLocation resource = getConverter().asNativeWorld(world).dimension().location();
        return getConverter().asNamespacedKey(resource);
    }

    @Deprecated
    @Override
    public NamespacedKey getNamespacedKey(Biome biome) {
        /*Registry<net.minecraft.world.level.biome.Biome> biomeRegistry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME);
        ResourceLocation resource = biomeRegistry.getKey(biome);

        System.out.println("Is biome registry frozen? " + Reflect.of(biomeRegistry).readField(ReflectionMappings.FROZEN_REGISTRY_FIELD, boolean.class)); // TODO: remove after debug

        if(resource == null) {
            throw new RuntimeException("Failed to retrieve resource location for biome!");
        }*/
        return biome.getKey();
    }

    public NativeTypeConversionsImpl getConverter() {
        return NativeTypeConversionsImpl.inst();
    }

}
