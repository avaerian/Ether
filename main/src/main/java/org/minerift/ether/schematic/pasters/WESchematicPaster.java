package org.minerift.ether.schematic.pasters;

import com.google.common.base.Preconditions;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.types.WorldEditSchematic;
import org.minerift.ether.math.Vec3i;

public class WESchematicPaster implements ISchematicPaster<WorldEditSchematic> {

    @Override
    public void paste(WorldEditSchematic schem, Vec3i pos, String worldName, SchematicPasteOptions options) {

        final World world = Bukkit.getWorld(worldName);
        Preconditions.checkNotNull(world, String.format("World %s could not be found!", worldName));

        try(EditSession editSession = WorldEdit.getInstance().newEditSession(BukkitAdapter.adapt(world))) {

            final Clipboard clipboard = schem.getClipboard();
            final ClipboardHolder holder = new ClipboardHolder(clipboard);

            final Vec3i.Mutable vecTo = options.offset.asMutable();
            vecTo.add(pos);

            final BlockVector3 to = BlockVector3.at(vecTo.getX(), vecTo.getY(), vecTo.getZ());

            Operation operation = holder.createPaste(editSession)
                    .to(to)
                    .copyBiomes(options.copyBiomes)
                    .copyEntities(options.copyEntities)
                    .ignoreAirBlocks(options.ignoreAirBlocks)
                    .build();

            Operations.completeLegacy(operation);

        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }

    }
}
