package org.minerift.ether.debug;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.types.Schematic;
import org.minerift.ether.util.BukkitUtils;
import org.minerift.ether.util.math.Vec3i;

import java.io.File;

// Class for testing the use of the refactored schematic API
public class SchematicDebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Please use this command as a player");
            return false;
        }

        final Player plr = (Player) sender;
        final Vec3i pos = BukkitUtils.asVec3i(plr.getLocation());
        final String worldName = plr.getWorld().getName();

        final String[] filePaths = {
                "C:\\Users\\avaer\\Downloads\\testisland.schem",
                "C:\\Users\\avaer\\Downloads\\testisland2.schem",
                "C:\\Users\\avaer\\Downloads\\testisland3.schem"
        };

        final File file = new File(filePaths[Integer.parseInt(args[0])]);

        // Attempt to paste
        Schematic.fromFile(file).handle((schem) -> {
            schem.paste(pos, worldName, SchematicPasteOptions.DEFAULT);
            plr.sendMessage("Schematic pasted successfully!");
        }, this::schemFail);

        return true;
    }

    private void schemFail(SchematicFileReadException ex) {
        ex.printStackTrace();
        Bukkit.broadcast(Component.text("Schematic failed to paste! Check console for details"));
    }
}
