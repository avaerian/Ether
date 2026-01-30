package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigReadException;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.dimension.Dimension;

public class ConfigReloadDebugCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        final MainConfig config = Ether.inst().getConfig(ConfigType.MAIN);
        boolean reload;
        try {
            reload = config.reload();
        } catch (ConfigReadException e) {
            reload = false;
        }
        if(reload) {
            sender.sendMessage("Config reloaded successfully!");

            MainConfig cfg = Ether.inst().getConfig(ConfigType.MAIN);
            for(Dimension dim : cfg.getDimensions().values()) {
                Ether.LOGGER.debug(dim.getName());
                Ether.LOGGER.debug("tileLenChunks: {}", dim.getTileLenChunks());
                Ether.LOGGER.debug("islandSpawnY: {}", dim.getIslandSpawnY());
                Ether.LOGGER.debug("tileAccessibleLenBlocks: {}", dim.getTileAccessibleLenBlocks());
            }
        }
        return reload;
    }
}
