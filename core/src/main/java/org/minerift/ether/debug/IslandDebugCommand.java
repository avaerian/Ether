package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.user.EtherUser;

public class IslandDebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player plr)) {
            sender.sendMessage("Must be player to execute command!");
            return false;
        }

        if(args.length == 0) {
            plr.sendMessage("No arguments found.");
            return false;
        }

        EtherUser user = Ether.getUserManager().getUser(plr.getUniqueId())
                .orElseThrow(() -> new UnsupportedOperationException(String.format("User %s not found!", plr.getUniqueId())));

        switch(args[0].toLowerCase()) {
            case "create"   -> Ether.getIslandManager().createIsland(user);
            case "delete"   -> {} //Ether.getIslandManager().deleteIsland();
            case "get"      -> {
                var optIsland = Ether.getIslandManager().getIslandAt(plr.getLocation());
                optIsland.ifPresentOrElse(
                        (island) -> plr.sendMessage("You are standing in island " + island.getTile() + " : " + island.getId()),
                        ()       -> plr.sendMessage("You aren't standing in any island!")
                );
            }
            default -> {
                plr.sendMessage(String.format("Unrecognized argument \"%s\"", args[0]));
                return false;
            }
        }

        return true;
    }
}
