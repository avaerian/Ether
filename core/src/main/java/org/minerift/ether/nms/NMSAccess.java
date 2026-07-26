package org.minerift.ether.nms;

import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.debug.Experimental;
import org.minerift.ether.debug.Experiments;
import org.minerift.ether.debug.NeedsReview;
import org.minerift.ether.debug.NeedsTesting;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.Chunk;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.world.EntityArchetype;
import org.minerift.ether.world.EntityLoadException;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface NMSAccess {

    // TODO: review; looking into local FixerUpper that emulates native MC FixerUpper
    <T extends Tag> T fixUpItemName(T nbt, int dataVersion);


    // move to World interface once introduced
    void addEntity(World world, EntityArchetype entity) throws EntityLoadException;

    default boolean tryAddEntity(World world, EntityArchetype entity) {
        try {
            addEntity(world, entity);
            return true;
        } catch(EntityLoadException e) {
            Ether.inst().getLogger().error("Failed to add entity {}", entity.getId(), e);
            return false;
        }
    }

    Dimension getDimFromWorld(World world); // move to World interface once introduced

    void clearChunk(Chunk chunk, boolean clearEntities);
    CompletableFuture<Void> clearChunks(Chunk c1, Chunk c2, boolean clearEntities);
    CompletableFuture<Void> clearChunks(ChunkGetter cg, Chunk c1, Chunk c2, boolean clearEntities);

    // move to World interface once introduced
    BlockState getBlockState(World world, Vec3 pos);
    BlockState setBlockState(World world, Vec3 pos, BlockState state);

    @NeedsTesting
    boolean isSuffocating(World world, Vec3 eyePos); // move to World interface once introduced
    //boolean isValidSpawn(World world, Vec3 eyePos, /*EntityType*/);

    void broadcastChunkBiomeUpdates(World world, List<Chunk> chunks);

    int getDataVersion(); // TODO: review for DataFixerUpper
    RegistryAccess registryAccess();
    NativeTypeConversions getConverter();
    @Experimental Experiments experiments();

    //void relightChunks(Collection<Vec2i> chunks);
    @Deprecated void relightChunks(Vec2i from, Vec2i to);

    /**
     * Relight chunks in a circular fashion, circling around the player to optimally relight the
     * chunks without initially relighting chunks far off in the distance.
     *
     * <p>
     * <b>NOTE:</b> Arguments are <u>not normalized</u>.
     *
     * @param world world to relight chunks in.
     * @param from  first chunk bound, presumably for a tile.
     * @param to    second chunk bound, presumably for a tile.
     * @param pos   center position, presumably the spawn position.
     */
    @NeedsTesting
    void relightChunksCircular(World world, Vec2i from, Vec2i to, Vec3 pos);

}
