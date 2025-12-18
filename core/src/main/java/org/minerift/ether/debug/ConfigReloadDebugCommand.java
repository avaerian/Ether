package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;

public class ConfigReloadDebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        final MainConfig config = Ether.inst().getConfig(ConfigType.MAIN);
        boolean reload = config.reload();
        if(reload) {
            sender.sendMessage("Config reloaded successfully!");

            Ether.inst().getLogger().info("tileSize: " + config.getTileLengthChunks());
            Ether.inst().getLogger().info("tileHeight: " + config.getTileHeight());
            Ether.inst().getLogger().info("tileAccessibleArea: " + config.getTileAccessibleAreaBlocks());
        }
        return reload;
    }
}
