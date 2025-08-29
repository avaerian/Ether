package org.minerift.ether.schematic.sponge;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.nms.world.*;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.SchematicPaster;
import org.minerift.ether.schematic.data.Pasters;

import static java.lang.String.format;

@SuppressWarnings("Duplicates")
public class SpongeSchematicPaster implements SchematicPaster<SpongeSchematic> {
    @Override
    public void paste(SpongeSchematic schem, Vec3i pasteLoc, String worldName, SchematicPasteOptions options) {
        World world = Bukkit.getWorld(worldName);
        ChunkGetter cg = ChunkGetter.SYNC; // TODO: make this an option

        Pasters.pasteBlockVolume(schem.getBlocks(), world, pasteLoc, cg);
    }

}
