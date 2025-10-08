package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.SchematicType;
import org.minerift.ether.schematic.data.BlockVolume;
import org.minerift.ether.schematic.data.Pasters;
import org.minerift.ether.schematic.sponge.SpongeSchematic;
import org.minerift.ether.schematic.transform.Axis;
import org.minerift.ether.schematic.transform.Rotate;

import java.io.File;
import java.util.Optional;

import static org.minerift.ether.nms.world.ChunkGetter.SYNC;
import static org.minerift.ether.schematic.data.BlockVolume.ROTATE_BLK_DIRS;
import static org.minerift.ether.util.BukkitUtils.asVec3i;

public class TransformDebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player plr)) {
            sender.sendMessage("You need to be a player to execute!");
            return false;
        }

        final String[] filePaths = {
                "C:\\Users\\avaer\\Downloads\\testisland.schem",
                "C:\\Users\\avaer\\Downloads\\testisland2.schem",
                "C:\\Users\\avaer\\Downloads\\testisland3.schem"
        };

        final File file = new File(filePaths[Integer.parseInt(args[0])]);

        Axis axis = switch (args[1]) {
            case "x" -> Axis.X;
            case "y" -> Axis.Y;
            case "z" -> Axis.Z;
            default -> throw new IllegalStateException("Unexpected value: " + args[1]);
        };

        Optional<Rotate.Angle> angle = Rotate.Angle.ofDeg(Integer.parseInt(args[2]));

        try {
            SpongeSchematic schem = (SpongeSchematic) Schematic.fromFile(SchematicType.SPONGE, file);
            BlockVolume bv = schem.getBlocks().transform(Rotate.of(axis, angle), ROTATE_BLK_DIRS);
            Pasters.pasteBlockVolume(bv, plr.getWorld(), asVec3i(plr.getLocation()), SYNC);
        } catch (SchematicReadException ex) {
            throw new RuntimeException(ex);
        }

        return true;
    }

}
