package org.minerift.ether.nms;

import org.jetbrains.annotations.Nullable;
import org.minerift.ether.nms.world.Biome;
import org.minerift.ether.nms.world.BlockState;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.Section;
import org.minerift.ether.world.BlockArchetype;

// NBS -> native block state
// NC  -> native chunk
// NCS -> native chunk section
public interface NativeTypeConversions<NBS, NC, NCS, NB> {

    NBS asNativeBlockState(String id) throws BlockStateNotFoundException;

    @Deprecated
    default NBS asNativeBlockState(BlockArchetype block) {
        // TODO: switch BlockArchetype to use BlockState instead of string id
        try {
            return asNativeBlockState(block.getData());
        } catch (BlockStateNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

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

    @Deprecated
    default BlockState<NBS> asBlockState(BlockArchetype block) {
        // TODO: switch BlockArchetype to use BlockState instead of string id
        try {
            return asBlockState(block.getData());
        } catch (BlockStateNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

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
