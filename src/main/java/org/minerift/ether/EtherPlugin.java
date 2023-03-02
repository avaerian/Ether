package org.minerift.ether;

import org.bukkit.plugin.java.JavaPlugin;
import org.minerift.ether.island.IslandManager;

import java.util.logging.Level;

public class EtherPlugin extends JavaPlugin {

    private static EtherPlugin INSTANCE = null;

    private IslandManager islandManager;

    @Override
    public void onLoad() {
        // TODO: look into when this method is called vs. onEnable()
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.islandManager = new IslandManager();

        getLogger().log(Level.INFO, "Ether plugin enabled!");
    }

    @Override
    public void onDisable() {

    }

    public static EtherPlugin getInstance() {
        return INSTANCE;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }
}
