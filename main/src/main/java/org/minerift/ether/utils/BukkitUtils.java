package org.minerift.ether.utils;

import org.bukkit.Location;
import org.minerift.ether.config.MainConfiguration;
import org.minerift.ether.island.Tile;

/**
 * Utility class for interfacing with the Bukkit API.
 * @author Avaerian
 */
public class BukkitUtils {

    // Get a tile from a Bukkit location
    public static Tile getTileAt(Location location) {
        final int TILE_SIZE = MainConfiguration.TILE_SIZE;
        int tileX = (int) Math.floor(location.getX() / TILE_SIZE);
        int tileZ = (int) Math.floor(location.getZ() / TILE_SIZE);
        return new Tile(tileX, tileZ);
    }

    // Get the top right corner Bukkit location from a tile
    public static Location getLocationAt(Tile tile) {
        // TODO: change constants to static; defer instantiation of these constants
        final int TILE_SIZE = MainConfiguration.TILE_SIZE;
        final int TILE_HEIGHT = MainConfiguration.TILE_HEIGHT;
        return new Location(null, tile.getX() * TILE_SIZE, TILE_HEIGHT, tile.getZ() * TILE_SIZE);
    }

}
