package org.minerift.ether.nms;

import org.bukkit.World;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.world.EntityArchetype;
import org.minerift.ether.world.EntityLoadException;

import java.util.concurrent.CompletableFuture;

public interface NMSAccess { // TODO: refactor to abstract class?

    // TODO: reconsider
    default Chunk getChunkAt(World world, int chunkX, int chunkZ) {
        return Chunk.of(world.getChunkAt(chunkX, chunkZ));
    }

    // TODO: reconsider
    default CompletableFuture<Chunk> getChunkAtAsync(World world, int chunkX, int chunkZ) {
        return world.getChunkAtAsync(chunkX, chunkZ)
                .thenApply(Chunk::of);
    }

    void addEntity(World world, EntityArchetype entity) throws EntityLoadException;

    default boolean tryAddEntity(World world, EntityArchetype entity) {
        try {
            addEntity(world, entity);
            return true;
        } catch(EntityLoadException ex) {
            // TODO: proper logging
            System.out.println("Failed to add entity: " + entity.getType());
            ex.printStackTrace();
            return false;
        }
    }

    RegistryAccess registryAccess();
    NativeTypeConversions getConverter();

    //void relightChunks(Set<Vec2i> chunks); // TODO: remove? may not be needed



}
