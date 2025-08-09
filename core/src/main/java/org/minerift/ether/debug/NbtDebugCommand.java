package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.world.ItemStack;
import org.minerift.ether.util.nbt.tags.StringTag;

@Debug
public class NbtDebugCommand implements CommandExecutor {

    // Cmd: /dbgnbt

    @EventHandler
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player plr)) {
            sender.sendMessage("Must be console to execute");
            return false;
        }

        if(args.length == 0) {
            sender.sendMessage("/dbgnbt <get|fix>");
            return false;
        }


        // - Test NBT for holding an item
        // - Test NBT translation between native and core
        // - Store island spec in NBT and get working

        switch (args[0]) {
            case "get" -> {
                ItemStack<?> item = ItemStack.of(plr.getInventory().getItemInMainHand());
                plr.sendMessage("NBT: " + item.getNbtTag().toString());
                System.out.println("NBT: " + item.getNbtTag().toString());

                plr.sendMessage(item.getResourceLocation());
            }
            case "fix" -> {
                String id;
                if(args.length == 2) {
                    id = args[1];
                } else {
                    ItemStack<?> item = ItemStack.of(plr.getInventory().getItemInMainHand());
                    plr.sendMessage("NBT: " + item.getNbtTag().toString());
                    System.out.println("NBT: " + item.getNbtTag().toString());
                    id = item.getResourceLocation();
                }

                plr.sendMessage(Ether.getNms().fixUpItemName(StringTag.valueOf(id), -1).getValue()); // TODO: shorten this line by making code better; this is utter garbage
            }
        }

        return true;
    }
}
