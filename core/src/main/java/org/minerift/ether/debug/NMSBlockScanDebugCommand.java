package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.world.ChunkGetter;

public class NMSBlockScanDebugCommand implements CommandExecutor {

    // Command format: /blockscan <mode> <diameter>
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Please use this command as a player");
            return false;
        }

        final Player plr = (Player) sender;

        String mode = "SEC"; // modes: "SEC", "CHUNK", "MULTI" -> default: SEC
        if(args.length >= 1) {
            mode = args[0].toUpperCase();
        }

        //final NMSAccess nms = EtherPlugin.getInstance().getNMS();
        final Experiments exp = Ether.inst().getNms().experiments();

        switch (mode) {
            case "SEC" -> exp.testIslandScanIdea(plr.getLocation());
            case "CHUNK" -> exp.testIslandScanIdeaFullChunk(plr.getLocation());
            case "MULTI" -> {
                int diameter = 1;
                if(args.length >= 2) {
                    diameter = Integer.parseInt(args[1]);
                }
                exp.testIslandScanIdeaMultiChunk(ChunkGetter.SYNC, plr.getLocation(), diameter);
            }
        }

        return true;
    }
}
