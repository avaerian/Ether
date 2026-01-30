package org.minerift.ether.dimension;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.minerift.ether.Ether;

public class Dimension {

    public static Dimension from(World world) {
        return Ether.inst().getNms().getDimFromWorld(world);
    }

    private final String name;
    private final NamespacedKey namespacedKey;
    private int tileLenChunks;
    private int tileAccessibleLenBlocks;
    private int islandSpawnY;

    public Dimension(String name, NamespacedKey namespacedKey, int tileLenChunks, int tileAccessibleLenBlocks, int islandSpawnY) {
        this.name = name;
        this.namespacedKey = namespacedKey;
        this.tileLenChunks = tileLenChunks;
        this.tileAccessibleLenBlocks = tileAccessibleLenBlocks;
        this.islandSpawnY = islandSpawnY;
    }

    public String getName() {
        return name;
    }

    public NamespacedKey getNamespacedKey() {
        return namespacedKey;
    }

    public int getTileLenChunks() {
        return tileLenChunks;
    }

    public int getTileLenBlocks() {
        return tileLenChunks * 16;
    }

    public void setTileLenChunks(int tileLenChunks) {
        this.tileLenChunks = tileLenChunks;
    }

    public int getIslandSpawnY() {
        return islandSpawnY;
    }

    public void setIslandSpawnY(int islandSpawnY) {
        this.islandSpawnY = islandSpawnY;
    }

    public int getTileAccessibleLenBlocks() {
        return tileAccessibleLenBlocks;
    }

    public void setTileAccessibleLenBlocks(int tileAccessibleLenBlocks) {
        this.tileAccessibleLenBlocks = tileAccessibleLenBlocks;
    }

    public World getWorld() {
        return Bukkit.getWorld("ether_" + name);
    }
}
