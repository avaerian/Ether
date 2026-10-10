package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.util.BukkitUtils;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.work.Work;

import java.io.File;

// Class for testing the use of the refactored schematic API
public class SchematicDebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Please use this command as a player");
            return false;
        }

        final Ether ether = Ether.inst();
        final Player plr = (Player) sender;
        final Vec3i pos = BukkitUtils.asVec3i(plr.getLocation());
        final String worldName = plr.getWorld().getName();

        final String[] filePaths = {
            "debug/testisland.schem",
            "debug/testisland2.schem",
            "debug/testisland3.schem",
        };
        final String file = filePaths[Integer.parseInt(args[0])];

        // Attempt to paste
        try {
            Schematic schem = Schematic.fromFile(ether.getPluginFile(file));
            Work<Void> pasteTask = schem.paste(pos, worldName, SchematicPasteOptions.DEFAULT)
                    .whenComplete((w) -> plr.sendMessage("Schematic pasted successfully!"));
            ether.getWorkQueue().enqueue(pasteTask);
        } catch (SchematicReadException ex) {
            plr.sendMessage("Schematic failed to read! Check console for details");
            ex.printStackTrace();
        }

        return true;
    }
}
