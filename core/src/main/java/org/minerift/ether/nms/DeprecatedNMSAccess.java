package org.minerift.ether.nms;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.minerift.ether.math.Vec3d;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.world.BlockArchetype;
import org.minerift.ether.world.EntityArchetype;

import java.lang.reflect.InvocationTargetException;
import java.util.List;


@Deprecated
// TODO: refactor so NMS bridge can be accessed directly?
public class DeprecatedNMSAccess {

    private String implVersion;
    private final DeprecatedNMSBridge bridge;
    //private final NMSBridge newBridge;
    private final RegistryAccess registryAccess;
    public DeprecatedNMSAccess() {
        this.implVersion = getImplVersion();

        // Attempt to load bridge
        try {
            this.bridge         = loadNmsImpl(DeprecatedNMSBridge.class, "DeprecatedNMSBridgeImpl");
            this.registryAccess = loadNmsImpl(RegistryAccess.class, "RegistryAccessImpl");
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    // TODO: move outside class to decouple from Bukkit and allow for unit testing?
    public static String getImplVersion() {
        String version = Bukkit.getServer().getClass().getPackageName();
        version = version.substring(version.lastIndexOf('.') + 2);
        return version;
    }

    // Assumes that the implVersion is already loaded appropriately
    private <T> T loadNmsImpl(Class<T> iface, String implName) throws ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        return loadNmsImpl(iface, implName, implVersion);
    }

    private <T> T loadNmsImpl(Class<T> iface, String implName, String version) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName("org.minerift.ether.nms.v" + version + "." + implName);
        return iface.cast(clazz.getConstructor().newInstance());
    }

    public DeprecatedNMSBridge getBridge() {
        return bridge;
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

    //public void testSetSectionBlocks(int cx, int sy, int cz, )

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

    public void testNewPartitionPaster(List<BlockArchetype> blocks, World world) {
        bridge.testNewPartitionPaster(blocks, world);
    }

    public NamespacedKey getBiomeKey(World world, int x, int y, int z) {
        return bridge.getBiomeAt(world, x, y, z);
    }

    public NamespacedKey getBiomeKey(Location loc) {
        return getBiomeKey(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public NamespacedKey getBiomeKey(World world, Vec3i pos) {
        return getBiomeKey(world, pos.getX(), pos.getY(), pos.getZ());
    }

    public NamespacedKey getBiomeKey(World world, Vec3d pos) {
        return getBiomeKey(world, (int)pos.getXd(), (int)pos.getYd(), (int)pos.getZd());
    }

    public NamespacedKey getNamespacedKey(ItemStack item) {
        return registryAccess.getNamespacedKey(item);
    }

    public NamespacedKey getNamespacedKey(BlockState blockState) {
        return registryAccess.getNamespacedKey(blockState);
    }

    public NamespacedKey getNamespacedKey(BlockData blockData) {
        return registryAccess.getNamespacedKey(blockData);
    }

    public NamespacedKey getDimNamespacedKey(World world) {
        return registryAccess.getDimNamespacedKey(world);
    }
}
