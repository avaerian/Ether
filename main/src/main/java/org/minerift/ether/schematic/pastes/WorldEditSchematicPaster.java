package org.minerift.ether.schematic.pastes;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Location;
import org.minerift.ether.schematic.types.WorldEditSchematic;


public class WorldEditSchematicPaster {

    public static void paste(WorldEditSchematic schematic, Location location) throws MaxChangedBlocksException {

        try(EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(location.getWorld()))) {

            Clipboard clipboard = schematic.getClipboard();
            ClipboardHolder holder = new ClipboardHolder(clipboard);
            Operation operation = holder.createPaste(editSession)
                    .to(clipboard.getOrigin().add(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                    .copyBiomes(false)
                    .copyEntities(true)
                    .ignoreAirBlocks(true)
                    .build();

            Operations.completeLegacy(operation);

        }

    }

}
