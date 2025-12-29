package org.minerift.ether.nms;

import org.bukkit.World;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.debug.Experiments;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.world.EntityArchetype;
import org.minerift.ether.world.EntityLoadException;

import java.util.concurrent.CompletableFuture;

public interface NMSAccess {

    // TODO: review; looking into local FixerUpper that emulates native MC FixerUpper
    <T extends Tag> T fixUpItemName(T nbt, int dataVersion);

    void addEntity(World world, EntityArchetype entity) throws EntityLoadException;

    default boolean tryAddEntity(World world, EntityArchetype entity) {
        try {
            addEntity(world, entity);
            return true;
        } catch(EntityLoadException ex) {
            // TODO: proper logging
            System.out.println("Failed to add entity: " + entity.getId());
            ex.printStackTrace();
            return false;
        }
    }

    void clearChunk(Chunk chunk, boolean clearEntities);
    CompletableFuture<Void> clearChunks(Chunk c1, Chunk c2, boolean clearEntities);
    CompletableFuture<Void> clearChunks(ChunkGetter cg, Chunk c1, Chunk c2, boolean clearEntities);

    /*default Chunk getChunkAt(World world, int chunkX, int chunkZ) {
        return Chunk.of(world.getChunkAt(chunkX, chunkZ));
    }

    default CompletableFuture<Chunk> getChunkAtAsync(World world, int chunkX, int chunkZ) {
        return world.getChunkAtAsync(chunkX, chunkZ)
                .thenApply(Chunk::of);
    }*/

    int getDataVersion(); // TODO: review for DataFixerUpper
    RegistryAccess registryAccess();
    NativeTypeConversions getConverter();
    @Experimental Experiments experiments();

    //void relightChunks(Set<Vec2i> chunks); // TODO: remove? may not be needed



}
