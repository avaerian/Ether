package org.minerift.ether.debug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;

public class NMSRegistryDebugCommand implements CommandExecutor {

    // Item is current item in hand
    // Biome is at player foot location
    // Block is at player foot location
    // Dim is at current player world
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        final String USAGE = "/testreg <item | biome | block | dim>";
        final Component USAGE_COMP = Component.text(USAGE).color(NamedTextColor.RED);

        if(!(sender instanceof Player plr)) {
            return false;
        }

        if(args.length == 0) {
            plr.sendMessage(USAGE_COMP);
            return false;
        }

        switch(args[0].toLowerCase()) {
            case "item" -> {
                ItemStack item = plr.getInventory().getItemInMainHand();
                plr.sendMessage(Ether.getNMS().getNamespacedKey(item).asString());
                plr.sendMessage(item.getItemMeta().getAsString());
            }
            case "block" -> {
                Block block = plr.getLocation().getBlock();
                plr.sendMessage(Ether.getNMS().getNamespacedKey(block.getState()).asString());
            }
            case "biome" -> {
                plr.sendMessage(Ether.getNMS().getBiomeKey(plr.getLocation()).asString());
            }
            case "dim" -> {
                plr.sendMessage(Ether.getNMS().getDimNamespacedKey(plr.getWorld()).asString());
            }
            default -> {
                plr.sendMessage(USAGE_COMP);
                return false;
            }
        }
        return true;
    }
}
