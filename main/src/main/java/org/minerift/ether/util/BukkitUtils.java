package org.minerift.ether.util;

import org.bukkit.Location;
import org.minerift.ether.util.math.Vec2i;
import org.minerift.ether.util.math.Vec3d;
import org.minerift.ether.util.math.Vec3i;

import static org.minerift.ether.config.deprecated.MainConfig.TILE_HEIGHT;
import static org.minerift.ether.config.deprecated.MainConfig.TILE_SIZE;

/**
 * Utility class for interfacing with the Bukkit API.
 * @author Avaerian
 */
public class BukkitUtils {

    // Get a tile from a Bukkit location
    public static Vec2i getTileAt(Location loc) {
        int tileX = (int) Math.floor(loc.getX() / TILE_SIZE);
        int tileZ = (int) Math.floor(loc.getZ() / TILE_SIZE);
        return new Vec2i(tileX, tileZ);
    }

    // Get the top right corner Vec3i location from a tile (world coordinates)
    public static Vec3i getVec3iAt(Vec2i tile) {
        return new Vec3i(tile.getX() * TILE_SIZE, TILE_HEIGHT, tile.getZ() * TILE_SIZE);
    }

    // Get the top right corner Bukkit location from a tile (world coordinates)
    public static Location getLocationAt(Vec2i tile) {
        final Vec3i loc = getVec3iAt(tile);
        return new Location(null, loc.getX(), loc.getY(), loc.getZ());
    }

    public static Vec3i asVec3i(Location loc) {
        return new Vec3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static Vec3d asVec3d(Location loc) {
        return new Vec3d(loc.getX(), loc.getY(), loc.getZ());
    }

}
