package org.minerift.ether.schematic.pasters;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.minerift.ether.schematic.types.WorldEditSchematic;
import org.minerift.ether.util.math.Vec3i;

public class WESchematicPaster implements ISchematicPaster<WorldEditSchematic> {

    @Override
    public void paste(WorldEditSchematic schem, Vec3i pos, String worldName) {

        final World world = Bukkit.getWorld(worldName);
        Preconditions.checkNotNull(world, String.format("World %s could not be found!", worldName));

        try(EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {

            Clipboard clipboard = schem.getClipboard();
            ClipboardHolder holder = new ClipboardHolder(clipboard);
            Operation operation = holder.createPaste(editSession)
                    .to(clipboard.getOrigin().add(pos.getX(), pos.getY(), pos.getZ()))
                    .copyBiomes(false)
                    .copyEntities(true)
                    .ignoreAirBlocks(true)
                    .build();

            Operations.completeLegacy(operation);

        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }

    }
}
