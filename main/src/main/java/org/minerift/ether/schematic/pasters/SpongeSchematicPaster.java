package org.minerift.ether.schematic.pasters;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.util.math.Vec3i;

public class SpongeSchematicPaster implements ISchematicPaster<SpongeSchematic> {

    @Override
    public void paste(SpongeSchematic schem, Vec3i pos, String worldName) {

        final World world = Bukkit.getWorld(worldName);
        Preconditions.checkNotNull(world, String.format("World %s could not be found!", worldName));

        // Translate to location + offset
        final Vec3i.Mutable worldPasteLoc = pos.asMutable();
        worldPasteLoc.add(schem.getOffset());

        schem.getBlocks().forEach(block -> block.getPos().add(worldPasteLoc));
        schem.getBiomes().forEach(biome -> biome.getPos().add(worldPasteLoc));
        schem.getEntities().forEach(entity -> entity.getPos().add(worldPasteLoc));

        // Lazily set blocks
        final NMSAccess nmsAccess = EtherPlugin.getInstance().getNMS();
        nmsAccess.setBlocksAsyncLazy(schem.getBlocks(), world);

        // Set biomes
        schem.getBiomes().forEach(biomeArchetype -> {
            final Biome biome = biomeArchetype.getBiome();
            final Vec3i biomePos = biomeArchetype.getPos();
            world.setBiome(biomePos.getX(), biomePos.getY(), biomePos.getZ(), biome);
        });

        // Place entities
        /*schem.getEntities().forEach(entityArchetype -> {
            nmsAccess.spawnEntity(entityArchetype, world);
        });*/

    }
}
