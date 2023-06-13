package org.minerift.ether;

import org.bukkit.plugin.java.JavaPlugin;
import org.minerift.ether.debug.NMSChunkDebugCommand;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.nms.NMS;

import java.util.logging.Level;

public class EtherPlugin extends JavaPlugin {

    private static EtherPlugin INSTANCE = null;

    private NMS nms;
    private IslandManager islandManager;

    @Override
    public void onLoad() {
        // TODO: look into when this method is called vs. onEnable()
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.nms = new NMS();

        this.islandManager = new IslandManager();


        // Register debug command
        getCommand("nmschunk").setExecutor(new NMSChunkDebugCommand());


        getLogger().log(Level.INFO, "Ether plugin enabled!");
    }

    @Override
    public void onDisable() {

    }

    public static EtherPlugin getInstance() {
        return INSTANCE;
    }

    public NMS getNMS() {
        return nms;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }
}
