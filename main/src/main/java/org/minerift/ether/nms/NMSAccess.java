package org.minerift.ether.nms;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.EntityArchetype;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class NMSAccess {

    private String implVersion;
    private final NMSBridge bridge;
    public NMSAccess() {
        this.implVersion = getImplVersion();

        // Attempt to load bridge
        try {
            this.bridge = getBridge(implVersion);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearChunk(Chunk chunk, boolean clearEntities) {
        bridge.fastClearChunk(chunk, clearEntities);
    }

    public void clearChunks(Chunk e1, Chunk e2, boolean clearEntities) {
        bridge.fastClearChunks(e1, e2, clearEntities);
    }

    public void clearChunksAsync(Chunk e1, Chunk e2, boolean clearEntities) {
        bridge.fastClearChunksAsync(e1, e2, clearEntities);
    }

    public void setBlocks(List<BlockArchetype> blocks, World world) {
        bridge.fastSetBlocks(blocks, world);
    }

    public void setBlocksAsync(List<BlockArchetype> blocks, World world) {
        bridge.fastSetBlocksAsync(blocks, world);
    }

    public void setBlocksAsyncLazy(List<BlockArchetype> blocks, World world) {
        bridge.fastSetBlocksAsyncLazy(blocks, world);
    }

    public void testIslandScanIdea(Location location) {
        bridge.testIslandScanIdea(location);
    }

    public void testIslandScanIdeaFullChunk(Location location) {
        bridge.testIslandScanIdeaFullChunk(location);
    }

    public void testIslandScanIdeaMultiChunk(Location location, int diameter) {
        bridge.testIslandScanIdeaMultiChunk(location, diameter);
    }

    public void spawnEntity(EntityArchetype entityArchetype, World world) {
        bridge.spawnEntity(entityArchetype, world);
    }

    private String getImplVersion() {
        if(implVersion == null) {
            String version = Bukkit.getServer().getClass().getPackageName();
            implVersion = version.substring(version.lastIndexOf('.') + 2);
        }
        return implVersion;
    }

    private NMSBridge getBridge(String version) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName("org.minerift.ether.nms.v" + version + ".NMSBridgeImpl");
        return (NMSBridge) clazz.getConstructor().newInstance();
    }

}
