package org.minerift.ether.nms.v1_20_R2;

import com.google.common.base.Preconditions;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.craftbukkit.v1_20_R2.CraftChunk;
import org.bukkit.craftbukkit.v1_20_R2.CraftRegistry;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockState;
import org.bukkit.craftbukkit.v1_20_R2.block.data.CraftBlockData;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_20_R2.inventory.CraftItemStack;
import org.minerift.ether.nms.BiomeNotFoundException;
import org.minerift.ether.nms.BlockStateNotFoundException;
import org.minerift.ether.nms.NativeTypeConversions;
import org.minerift.ether.nms.v1_20_R2.data.*;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.Section;
import org.minerift.ether.util.Note;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.*;
import org.minerift.ether.util.nbt.tags.array.ByteArrayTag;
import org.minerift.ether.util.nbt.tags.array.IntArrayTag;
import org.minerift.ether.util.nbt.tags.array.LongArrayTag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;
import org.minerift.ether.util.nbt.tags.container.ListTag;
import org.minerift.ether.util.reflect.Reflect;
import org.minerift.ether.world.BlockEntityArchetype;

import java.util.Map;

public class NativeTypeConversionsImpl implements NativeTypeConversions
        <net.minecraft.world.level.block.state.BlockState,
        LevelChunk, LevelChunkSection, Holder<Biome>,
                ItemStack> {

    // TODO: change to INSTANCE = new NativeTypeConversionsImpl() ??
    private static NativeTypeConversionsImpl INST;

    public static NativeTypeConversionsImpl inst() {
        if(INST == null) {
            INST = new NativeTypeConversionsImpl();
        }
        return INST;
    }

    public Tag asTag(net.minecraft.nbt.Tag nativeTag) {
        if(nativeTag == null) {
            return null;
        }

        return new TagBuilderVisitor().visit(nativeTag);
    }

    public net.minecraft.nbt.Tag asNativeTag(Tag tag) {
        if(tag == null) {
            return null;
        }

        return switch (tag) {

            case EndTag ignored -> net.minecraft.nbt.EndTag.INSTANCE;

            // Primitives
            case ByteTag t   -> net.minecraft.nbt.ByteTag.valueOf( t.getAsByte() );
            case ShortTag t  -> net.minecraft.nbt.ShortTag.valueOf( t.getAsShort() );
            case IntTag t    -> net.minecraft.nbt.IntTag.valueOf( t.getAsInt() );
            case LongTag t   -> net.minecraft.nbt.LongTag.valueOf( t.getAsLong() );
            case FloatTag t  -> net.minecraft.nbt.FloatTag.valueOf( t.getAsFloat() );
            case DoubleTag t -> net.minecraft.nbt.DoubleTag.valueOf( t.getAsDouble() );

            case StringTag t -> net.minecraft.nbt.StringTag.valueOf( t.getStrVal() );

            // Arrays
            case ByteArrayTag t -> new net.minecraft.nbt.ByteArrayTag( t.getValue() );
            case IntArrayTag t  -> new net.minecraft.nbt.IntArrayTag( t.getValue() );
            case LongArrayTag t -> new net.minecraft.nbt.LongArrayTag( t.getValue() );

            // Collections
            case ListTag<?> t -> {
                final net.minecraft.nbt.ListTag nativeTag = new net.minecraft.nbt.ListTag();
                t.getValue().forEach(tagInList -> nativeTag.add(asNativeTag(tagInList)));
                yield nativeTag;
            }

            case CompoundTag t -> {
                final net.minecraft.nbt.CompoundTag nativeTag = new net.minecraft.nbt.CompoundTag();
                for(Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                    nativeTag.put(entry.getKey(), asNativeTag(entry.getValue()));
                }
                yield nativeTag;
            }

            // TODO: review and update this
            default -> throw new IllegalStateException("Unexpected value: " + tag);
        };
    }

    // Returns the native BlockState for BlockData (null if data is null)
    public net.minecraft.world.level.block.state.BlockState asNativeBlockState(BlockData data) {
        return data == null ? null : ((CraftBlockData)data).getState();
    }

    public net.minecraft.world.level.block.state.BlockState asNativeBlockState(org.bukkit.block.BlockState data) {
        return ((CraftBlockState)data).getHandle();
    }

    // Returns the fallback if the block state can't be read
    public net.minecraft.world.level.block.state.BlockState
    asNativeBlockState(String id, net.minecraft.world.level.block.state.BlockState fallback) {
        try {
            return asNativeBlockState(id);
        } catch (BlockStateNotFoundException e) {
            return fallback;
        }
    }

    @Override
    public org.minerift.ether.nms.world.ItemStack<ItemStack> asItemStack(org.bukkit.inventory.ItemStack bukkitItemStack) {
        ItemStack handle = ((CraftItemStack)bukkitItemStack).handle;
        return new ItemStackImpl(handle);
    }

    @Override
    public org.minerift.ether.nms.world.ItemStack<ItemStack> asItemStack(ItemStack nativeItemStack) {
        return new ItemStackImpl(nativeItemStack);
    }

    @Override
    public ItemStack asNativeItemStack(CompoundTag nbt) {
        net.minecraft.nbt.CompoundTag nativeTag = (net.minecraft.nbt.CompoundTag) asNativeTag(nbt);
        return ItemStack.of(nativeTag);
    }

    @Override
    public net.minecraft.world.level.block.state.BlockState asNativeBlockState(String data) throws BlockStateNotFoundException {
        try {
            // Courtesy of Bukkit
            StringReader reader = new StringReader(data);
            BlockStateParser.BlockResult arg = BlockStateParser.parseForBlock(BuiltInRegistries.BLOCK.asLookup(), reader, false);
            Preconditions.checkArgument(!reader.canRead(), "Spurious trailing data: " + data);

            return arg.blockState();
        } catch (CommandSyntaxException ex) {
            throw new BlockStateNotFoundException("Failed to read string data as block state: ", ex);
        }
    }

    /*@Override
    public BlockState<net.minecraft.world.level.block.state.BlockState> asBlockState(String id) {
        return BlockStateImpl.of(id);
    }*/

    @Override
    public BlockState<net.minecraft.world.level.block.state.BlockState> asBlockState(net.minecraft.world.level.block.state.BlockState nativeState) {
        return BlockStateImpl.of(nativeState);
    }

    @Override
    public org.minerift.ether.nms.world.Biome<Holder<Biome>> asBiome(String id) throws BiomeNotFoundException {
        return BiomeImpl.of(id);
    }

    @Override
    public Holder.Reference<Biome> asNativeBiome(String id) throws BiomeNotFoundException {
        ResourceLocation biomeRes = new ResourceLocation(id);
        return asNativeBiome(ResourceKey.create(Registries.BIOME, biomeRes));

    }

    public Holder.Reference<Biome> asNativeBiome(ResourceKey<Biome> biomeKey) throws BiomeNotFoundException {
        try {
            Registry<Biome> biomeRegistry = MinecraftServer.getServer()
                    .registryAccess()
                    .registryOrThrow(Registries.BIOME);

            return biomeRegistry.getHolderOrThrow(biomeKey);
        } catch (IllegalArgumentException ex) {
            throw new BiomeNotFoundException("Biome " + biomeKey.location() + " not found", ex); // TODO: review
        }
    }

    @Override
    public org.minerift.ether.nms.world.Biome<Holder<Biome>> asBiome(Holder<Biome> nativeBiome) throws BiomeNotFoundException {
        return BiomeImpl.of(nativeBiome.unwrapKey().orElseThrow()); // TODO: review exception thrown
    }

    @Note("Position needs to be proper world coordinates, instead of normalized")
    public BlockEntity asNativeBlockEntity(BlockEntityArchetype blockEntity) throws BlockStateNotFoundException {
        net.minecraft.world.level.block.state.BlockState state = (net.minecraft.world.level.block.state.BlockState) blockEntity.getState().asNative();
        BlockPos pos = new BlockPos(blockEntity.getX(), blockEntity.getY(), blockEntity.getZ());
        return ((EntityBlock) (state.getBlock())).newBlockEntity(pos, state);
    }

    public ItemStack asNativeItem(org.bukkit.inventory.ItemStack item) {
        return ((CraftItemStack)item).handle;
    }

    // Nice to have if we ever need this
    public SoundEvent asNativeSound(Sound sound) {
        Registry<SoundEvent> soundRegistry = CraftRegistry.getMinecraftRegistry(Registries.SOUND_EVENT);
        return soundRegistry.get(asNativeResourceLoc(sound.getKey()));
    }

    public Entity asNativeEntity(org.bukkit.entity.Entity entity) {
        return ((CraftEntity)entity).getHandle();
    }

    public ResourceLocation asNativeResourceLoc(NamespacedKey key) {
        return new ResourceLocation(key.asString());
    }

    public ServerLevel asNativeWorld(World world) {
        return ((CraftWorld)world).getHandle();
    }

    public Biome asNativeBiome(NamespacedKey key) {
        Registry<Biome> biomeRegistry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME);
        return biomeRegistry.get(asNativeResourceLoc(key));
    }

    public LevelChunk asNativeChunk(org.bukkit.Chunk chunk) {
        return (LevelChunk) asNativeChunkAccess(chunk);
    }

    @Override
    public Chunk<net.minecraft.world.level.block.state.BlockState, LevelChunk, LevelChunkSection, Holder<Biome>> asChunk(LevelChunk nativeChunk) {
        return ChunkImpl.of(nativeChunk);
    }

    @Override
    public Section<net.minecraft.world.level.block.state.BlockState, LevelChunk, LevelChunkSection, Holder<Biome>> asChunkSection(LevelChunkSection nativeSection, int index) {
        return new SectionImpl(nativeSection, index);
    }

    public ChunkAccess asNativeChunkAccess(org.bukkit.Chunk chunk) {
        return asNativeChunkAccess(chunk, ChunkStatus.FULL);
    }

    public ChunkAccess asNativeChunkAccess(org.bukkit.Chunk chunk, ChunkStatus status) {
        return ((CraftChunk)chunk).getHandle(status);
    }

    public NamespacedKey asNamespacedKey(Biome nativeBiome) {
        Registry<Biome> biomeRegistry = MinecraftServer.getServer().registryAccess().registryOrThrow(Registries.BIOME);
        ResourceLocation resource = biomeRegistry.getKey(nativeBiome);

        System.out.println("Is biome registry frozen? " + Reflect.of(biomeRegistry).readField(ReflectionMappings.FROZEN_REGISTRY_FIELD_NAME, boolean.class)); // TODO: remove after debug

        if(resource == null) {
            throw new RuntimeException("Failed to retrieve resource location for biome!");
        }
        return asNamespacedKey(resource);
    }

    public NamespacedKey asNamespacedKey(ResourceLocation key) {
        return NamespacedKey.fromString(key.toString());
    }
}
