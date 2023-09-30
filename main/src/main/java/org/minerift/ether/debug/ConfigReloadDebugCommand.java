package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;

import static org.minerift.ether.Ether.getLogger;

public class ConfigReloadDebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        final MainConfig config = Ether.getConfig(ConfigType.MAIN);
        boolean reload = config.reload();
        if(reload) {
            sender.sendMessage("Config reloaded successfully!");

            getLogger().info("tileSize: " + config.getTileSize());
            getLogger().info("tileHeight: " + config.getTileHeight());
            getLogger().info("tileAccessibleArea: " + config.getTileAccessibleArea());
        }
        return reload;
    }
}
