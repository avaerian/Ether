package org.minerift.ether.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.minerift.ether.Ether;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.math.Maths;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3d;
import org.minerift.ether.math.Vec3i;

/**
 * Utility class for interfacing with the Bukkit API.
 * @author Avaerian
 */
public class BukkitUtils {

    public static String getImplVersionStr() {
        String version = Bukkit.getServer().getClass().getPackageName();
        version = version.substring(version.lastIndexOf('.') + 2);
        return version;
    }

    @Deprecated
    public static String dumpItemStack(ItemStack item) {
        NamespacedKey key = Ether.inst().getNms().registryAccess().getNamespacedKey(item);
        return key.asString() + item.getItemMeta().getAsString();
    }

    // Get a tile from a Bukkit location
    public static Vec2i getTileAt(Location loc) {
        return Maths.getTileAt(Dimension.from(loc.getWorld()), loc.getBlockX(), loc.getBlockZ());
    }

    // Get the top right corner Bukkit location from a tile (world coordinates)
    public static Location getLocationAt(Vec2i tile) {
        return getLocationAt(null, tile);
    }

    public static Location getLocationAt(World world, Vec2i tile) {
        final Vec3i vec = Maths.getVec3iAt(Dimension.from(world), tile);
        return new Location(null, vec.getX(), vec.getY(), vec.getZ());
    }

    public static Vec3i asVec3i(Location loc) {
        return new Vec3i(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public static Vec3d asVec3d(Location loc) {
        return new Vec3d(loc.getX(), loc.getY(), loc.getZ());
    }

    public static Location asBukkitLocation(World world, Vec3i vec) {
        return new Location(world, vec.getX(), vec.getY(), vec.getZ());
    }

    public static Location asBukkitLocation(World world, org.minerift.ether.world.Location loc) {
        return new Location(
                world, loc.getXd(), loc.getYd(), loc.getZd(),
                (float) loc.getYaw(), (float) loc.getPitch());
    }

}
