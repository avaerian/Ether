package org.minerift.ether.schematic.pastes;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.util.math.Vec3i;
import org.minerift.ether.world.BiomeArchetype;

public class SpongeSchematicPaster {

    public void paste(SpongeSchematic.Builder schematic, Location location) {

        final NMSAccess nmsAccess = EtherPlugin.getInstance().getNMS();
        final World world = location.getWorld();

        // Translate to location + offset
        final Vec3i.Mutable offset = new Vec3i.Mutable(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        //offset.add(schematic.getOffset());

        schematic.getBlocks().forEach(block -> block.getPos().add(offset));
        schematic.getBiomes().forEach(biome -> biome.getPos().add(offset));
        schematic.getEntities().forEach(entity -> entity.getPos().add(offset));

        // Lazily set blocks
        nmsAccess.setBlocksAsyncLazy(schematic.getBlocks(), world);

        // Set biomes
        schematic.getBiomes().forEach(biomeArchetype -> {
            final Biome biome = biomeArchetype.getBiome();
            final Vec3i pos = biomeArchetype.getPos();
            world.setBiome(pos.getX(), pos.getY(), pos.getZ(), biome);
        });

    }
}
