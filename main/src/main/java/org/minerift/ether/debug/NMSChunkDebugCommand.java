package org.minerift.ether.debug;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.nms.NMSAccess;

public class NMSChunkDebugCommand implements CommandExecutor {


    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Please use this command as a player");
            return false;
        }

        int diameter = 1;
        if(args.length >= 1) {
            diameter = Integer.parseInt(args[0]);
        }

        // TODO: mode doesn't matter as of right now
        String mode = "CLEAR"; // modes: "CLEAR", "REGEN", "ASYNC" -> default: clear
        if(args.length >= 2) {
            mode = args[1].toUpperCase();
        }

        Player plr = (Player) sender;
        World world = plr.getWorld();
        //NMSAccess nmsAccess = EtherPlugin.getInstance().getNMS();
        final NMSAccess nmsAccess = Ether.getNMS();

        int centerX = plr.getChunk().getX();
        int centerZ = plr.getChunk().getZ();

        int radius = (diameter - 1) / 2;

        Chunk e1 = world.getChunkAt(centerX - radius, centerZ - radius);
        Chunk e2 = world.getChunkAt(centerX + radius, centerZ + radius);

        // Perform action
        switch(mode) {
            case "ASYNC" -> nmsAccess.clearChunksAsync(e1, e2, true);
            default -> nmsAccess.clearChunks(e1, e2, true);
        }

        return true;
    }

}