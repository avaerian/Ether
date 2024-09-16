package org.minerift.ether.nms.v1_19_R3;

import net.kyori.adventure.sound.Sound;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_19_R3.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R3.CraftSound;
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R3.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_19_R3.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.world.BlockArchetype;

import java.util.Map;

public class NativeTypeConversions {

    public static net.minecraft.nbt.Tag toNative(Tag tag) {
        if(tag == null) {
            return null;
        }

        return switch (tag.getTagType()) {

            case END_TAG -> net.minecraft.nbt.EndTag.INSTANCE;

            // Primitives
            case BYTE_TAG   -> net.minecraft.nbt.ByteTag.valueOf(((ByteTag)tag).getValue());
            case SHORT_TAG  -> net.minecraft.nbt.ShortTag.valueOf(((ShortTag)tag).getValue());
            case INT_TAG    -> net.minecraft.nbt.IntTag.valueOf(((IntTag)tag).getValue());
            case LONG_TAG   -> net.minecraft.nbt.LongTag.valueOf(((LongTag)tag).getValue());
            case FLOAT_TAG  -> net.minecraft.nbt.FloatTag.valueOf(((FloatTag)tag).getValue());
            case DOUBLE_TAG -> net.minecraft.nbt.DoubleTag.valueOf(((DoubleTag)tag).getValue());
            case STRING_TAG -> net.minecraft.nbt.StringTag.valueOf(((StringTag)tag).getValue());

            // Arrays
            case BYTE_ARRAY_TAG -> new net.minecraft.nbt.ByteArrayTag(((ByteArrayTag)tag).getValue());
            case INT_ARRAY_TAG  -> new net.minecraft.nbt.IntArrayTag(((IntArrayTag)tag).getValue());
            case LONG_ARRAY_TAG -> new net.minecraft.nbt.LongArrayTag(((LongArrayTag)tag).getValue());

            // Collections
            case LIST_TAG -> {
                final ListTag listTag = ((ListTag) tag);
                final net.minecraft.nbt.ListTag nativeTag = new net.minecraft.nbt.ListTag();
                listTag.getValue().forEach(tagInList -> nativeTag.add(toNative(tagInList)));
                yield nativeTag;
            }

            case COMPOUND_TAG -> {
                final CompoundTag compoundTag = (CompoundTag) tag;
                final net.minecraft.nbt.CompoundTag nativeTag = new net.minecraft.nbt.CompoundTag();
                for(Map.Entry<String, Tag> entry : compoundTag.getValue().entrySet()) {
                    nativeTag.put(entry.getKey(), toNative(entry.getValue()));
                }
                yield nativeTag;
            }
        };
    }

    // Returns the native BlockState for BlockData (null if data is null)
    public static BlockState toNative(BlockData data) {
        return data == null ? null : ((CraftBlockData)data).getState();
    }

    public static BlockState toNative(org.bukkit.block.BlockState data) {
        return ((CraftBlockState)data).getHandle();
    }

    // Returns the fallback if the block state can't be read
    public static BlockState toNativeBlockState(String id, BlockState fallback) {
        try {
            return toNative(Bukkit.createBlockData(id));
        } catch (IllegalArgumentException ex) {
            return fallback;
        }
    }

    public static BlockState toNativeBlockState(String id) {
        return toNativeBlockState(id, null);
    }

    public static BlockState toNative(BlockArchetype block) {
        return toNativeBlockState(block.getId());
    }

    public static BlockState toNative(BlockArchetype block, BlockState fallback) {
        return toNativeBlockState(block.getId(), fallback);
    }

    public static ItemStack toNative(org.bukkit.inventory.ItemStack item) {
        return ((CraftItemStack)item).handle;
    }

    // For some reason if I ever need this?
    public static SoundEvent toNative(Sound sound) {
        return CraftSound.getSoundEffect(sound.name().asString());
    }

    public static Entity toNative(org.bukkit.entity.Entity entity) {
        return ((CraftEntity)entity).getHandle();
    }

    public static ResourceLocation toNative(NamespacedKey key) {
        return new ResourceLocation(key.asString());
    }

    public static ServerLevel toNative(World world) {
        return ((CraftWorld)world).getHandle();
    }

    public static Biome toNativeBiome(NamespacedKey key) {
        Registry<Biome> biomeRegistry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME);
        return biomeRegistry.get(toNative(key));
    }

    public static LevelChunk toNativeChunk(Chunk chunk) {
        return (LevelChunk) toNativeChunkAccess(chunk);
    }

    public static ChunkAccess toNativeChunkAccess(Chunk chunk) {
        return ((CraftChunk)chunk).getHandle(ChunkStatus.FULL);
    }

    public static NamespacedKey fromNative(Biome biome) {
        Registry<Biome> biomeRegistry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME);
        ResourceLocation resource = biomeRegistry.getKey(biome);

        System.out.println("Is biome registry frozen? " + Reflect.of(biomeRegistry).readField(ReflectionMappings.FROZEN_REGISTRY_FIELD, boolean.class)); // TODO: remove after debug

        if(resource == null) {
            throw new RuntimeException("Failed to retrieve resource location for biome!");
        }
        return fromNative(resource);
    }

    public static NamespacedKey fromNative(ResourceLocation key) {
        return NamespacedKey.fromString(key.toString());
    }
}
