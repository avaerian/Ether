package org.minerift.ether.nms;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.minerift.ether.util.math.Vec3i;
import org.minerift.ether.world.QueuedBlock;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class NMS {

    private String implVersion;
    private NMSBridge bridge;

    public NMS() {
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

    public void setBlocks(Set<QueuedBlock> blocks, Location location) {
        bridge.fastSetBlocks(blocks, location, new Vec3i(0, 0, 0));
    }

    public void setBlocksAsync(Set<QueuedBlock> blocks, Location location) {
        bridge.fastSetBlocksAsync(blocks, location, new Vec3i(0, 0, 0));
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
