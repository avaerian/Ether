package org.minerift.ether.dimension;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.minerift.ether.Ether;
import org.minerift.ether.debug.NeedsReview;
import org.minerift.ether.island.CobblestoneGen;

@Getter @Setter @AllArgsConstructor
public class Dimension {

    public static Dimension from(World world) {
        return Ether.inst().getNms().getDimFromWorld(world);
    }

    public static Dimension from(String id) {
        return from(Bukkit.getWorld(id));
    }

    @NeedsReview
    public static Dimension from(NamespacedKey key) {
        return from(Bukkit.getWorld(key));
    }

    private final String name;
    private final NamespacedKey namespacedKey;
    private CobblestoneGen cobblestoneGen;
    private int tileLenChunks;
    private int tileAccessibleLenBlocks;
    private int islandSpawnY;

    public String getResourceLocation() {
        return namespacedKey.toString();
    }

    public int getTileLenBlocks() {
        return tileLenChunks * 16;
    }

    public World getWorld() {
        return Bukkit.getWorld("ether_" + name);
    }
}
