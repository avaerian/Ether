package org.minerift.ether.schematic.pasters;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.types.SpongeSchematic;
import org.minerift.ether.math.Vec3i;

public class SpongeSchematicPaster implements ISchematicPaster<SpongeSchematic> {

    @Override
    public void paste(SpongeSchematic schem, Vec3i pos, String worldName, SchematicPasteOptions options) {

        final World world = Bukkit.getWorld(worldName);
        Preconditions.checkNotNull(world, String.format("World %s could not be found!", worldName));

        // Translate to location + offset
        final Vec3i.Mutable worldPasteLoc = pos.asMutable();
        worldPasteLoc.add(schem.getOffset());

        // TODO: ignore air blocks based on options

        // Lazily set blocks
        schem.getBlocks().forEach(block -> block.getPos().add(worldPasteLoc)); // translate to proper pos
        //final NMSAccess nmsAccess = EtherPlugin.getInstance().getNMS();
        final NMSAccess nmsAccess = Ether.getNMS();
        nmsAccess.setBlocksAsyncLazy(schem.getBlocks(), world);

        // Set biomes
        if(options.copyBiomes) {
            schem.getBiomes().forEach(biomeArchetype -> {
                biomeArchetype.getPos().add(worldPasteLoc); // translate to proper pos
                final Biome biome = biomeArchetype.getBiome();
                final Vec3i biomePos = biomeArchetype.getPos();
                world.setBiome(biomePos.getX(), biomePos.getY(), biomePos.getZ(), biome);
            });
        }

        if(options.copyEntities) {
            schem.getEntities().forEach(entity -> entity.getPos().add(worldPasteLoc)); // translate to proper pos
            // TODO: Place entities
            /*schem.getEntities().forEach(entityArchetype -> {
                nmsAccess.spawnEntity(entityArchetype, world);
            });*/
        }

    }
}
