package org.minerift.ether.nms;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import org.minerift.ether.nms.world.*;
import org.minerift.ether.util.nbt.snbt.Snbt;
import org.minerift.ether.util.nbt.snbt.UnexpectedTokenException;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.nbt.tags.container.CompoundTag;

import static org.minerift.ether.util.nbt.tags.PrimitiveTagType.COMPOUND;

// NBS -> native block state
// NC  -> native chunk
// NCS -> native chunk section
public interface NativeTypeConversions<NBS, NC, NCS, NB, NIS> {

    default ItemStack<NIS> asItemStack(String snbt) {
        return asItemStack(asNativeItemStack(snbt));
    }

    default ItemStack<NIS> asItemStack(CompoundTag nbt) {
        return asItemStack(asNativeItemStack(nbt));
    }

    ItemStack<NIS> asItemStack(org.bukkit.inventory.ItemStack bukkitItemStack);
    ItemStack<NIS> asItemStack(NIS nativeItemStack);

    default NIS asNativeItemStack(String snbt) {
        Tag tag;
        try {
            tag = Snbt.readTag(snbt);
        } catch (UnexpectedTokenException ex) {
            throw new RuntimeException(ex);
        }
        Preconditions.checkArgument(tag.is(COMPOUND));

        return asNativeItemStack((CompoundTag) tag);
    }

    NIS asNativeItemStack(CompoundTag nbt);


    NBS asNativeBlockState(String id) throws BlockStateNotFoundException;

    default BlockState<NBS> asBlockState(String id) throws BlockStateNotFoundException {
        return asBlockState(asNativeBlockState(id));
    }

    default BlockState<NBS> asBlockState(String id, @Nullable String fallback) {
        try {
            return asBlockState(id);
        } catch (BlockStateNotFoundException ex) {
            try {
                if(fallback == null) {
                    return null;
                }
                return asBlockState(fallback);
            } catch (BlockStateNotFoundException ex2) {
                throw new RuntimeException("Failed to load block state " + id + " and fallback state " + fallback, ex2);
            }
        }
    }

    BlockState<NBS> asBlockState(NBS nativeState);

    NB asNativeBiome(String id) throws BiomeNotFoundException;

    Biome<NB> asBiome(NB nativeBiome) throws BiomeNotFoundException;

    Biome<NB> asBiome(String id) throws BiomeNotFoundException;

    default Biome<NB> asBiome(String id, String fallback) {
        try {
            return asBiome(id);
        } catch (BiomeNotFoundException ex) {
            if(fallback == null) {
                return null;
            }
            try {
                return asBiome(fallback);
            } catch (BiomeNotFoundException ex2) {
                throw new RuntimeException("Failed to load biome " + id + " and fallback " + fallback + "; not found", ex2);
            }
        }
    }

    

    NC asNativeChunk(org.bukkit.Chunk bukkitChunk);

    Chunk<NBS, NC, NCS, NB> asChunk(NC nativeChunk);

    default Chunk<NBS, NC, NCS, NB> asChunk(org.bukkit.Chunk bukkitChunk) {
        NC nativeChunk = asNativeChunk(bukkitChunk);
        return asChunk(nativeChunk);
    }

    Section<NBS, NC, NCS, NB> asChunkSection(NCS nativeSection, int index);

}
