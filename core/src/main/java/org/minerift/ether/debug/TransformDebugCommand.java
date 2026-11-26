package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.SchematicType;
import org.minerift.ether.schematic.data.BlockVolume;
import org.minerift.ether.schematic.sponge.Pasters;
import org.minerift.ether.schematic.sponge.SpongeSchematic;
import org.minerift.ether.schematic.transform.Axis;
import org.minerift.ether.schematic.transform.Rotate;
import org.minerift.ether.work.Work;

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

        final Ether ether = Ether.inst();
        final String[] filePaths = {
            "debug/testisland.schem",
            "debug/testisland2.schem",
            "debug/testisland3.schem",
        };
        final String file = filePaths[Integer.parseInt(args[0])];

        Axis axis = switch (args[1]) {
            case "x", "X" -> Axis.X;
            case "y", "Y" -> Axis.Y;
            case "z", "Z" -> Axis.Z;
            default -> throw new IllegalStateException("Unexpected value: " + args[1]);
        };

        Optional<Rotate.Angle> angle = Rotate.Angle.ofDeg(Integer.parseInt(args[2]));

        try {
            SpongeSchematic schem = Schematic.fromFile(SchematicType.SPONGE, ether.getPluginFile(file));
            BlockVolume bv = schem.getBlocks().transform(Rotate.of(axis, angle), ROTATE_BLK_DIRS);

            Work<Void> pasteTask = Pasters.pasteBlockVolume(bv, plr.getWorld(), asVec3i(plr.getLocation()), SYNC)
                    .whenComplete((w) -> plr.sendMessage("Rotated BlockVolume pasted successfully!"));
            ether.getWorkQueue().enqueue(pasteTask);
        } catch (SchematicReadException ex) {
            throw new RuntimeException(ex);
        }

        return true;
    }

}
