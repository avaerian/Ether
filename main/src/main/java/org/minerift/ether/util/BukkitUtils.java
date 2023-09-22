package org.minerift.ether.util;

import org.bukkit.Location;
import org.minerift.ether.Ether;
import org.minerift.ether.config.types.ConfigType;
import org.minerift.ether.config.types.MainConfig;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3d;
import org.minerift.ether.math.Vec3i;

/**
 * Utility class for interfacing with the Bukkit API.
 * @author Avaerian
 */
public class BukkitUtils {

    // Get a tile from a Bukkit location
    public static Vec2i getTileAt(Location loc) {
        final MainConfig config = Ether.getConfig(ConfigType.MAIN);
        int tileX = (int) Math.floor(loc.getX() / config.getTileSize());
        int tileZ = (int) Math.floor(loc.getZ() / config.getTileSize());
        return new Vec2i(tileX, tileZ);
    }

    // Get the top right corner Vec3i location from a tile (world coordinates)
    // TODO: move to Maths.class
    public static Vec3i getVec3iAt(Vec2i tile) {
        final MainConfig config = Ether.getConfig(ConfigType.MAIN);
        return new Vec3i(tile.getX() * config.getTileSize(), config.getTileHeight(), tile.getZ() * config.getTileSize());
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
