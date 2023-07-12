package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.schematic.pastes.SpongeSchematicPaster;
import org.minerift.ether.schematic.readers.SpongeSchematicReader;
import org.minerift.ether.schematic.types.SpongeSchematic;

import java.io.File;
import java.io.IOException;

public class SchematicDebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof ConsoleCommandSender) {
            return false;
        }

        final NMSAccess nmsAccess = EtherPlugin.getInstance().getNMS();
        Player plr = (Player) sender;

        final String[] filePaths = {
            "C:\\Users\\avaer\\Downloads\\testisland.schem",
            "C:\\Users\\avaer\\Downloads\\testisland2.schem",
            "C:\\Users\\avaer\\Downloads\\testisland3.schem"
        };

        final File file = new File(filePaths[Integer.parseInt(args[0])]);
        try {

            SpongeSchematic.Builder builder = new SpongeSchematic.Builder();
            SpongeSchematicReader.read(file, builder);
            SpongeSchematicPaster paster = new SpongeSchematicPaster();
            paster.paste(builder, plr.getLocation());

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return true;
    }
}
