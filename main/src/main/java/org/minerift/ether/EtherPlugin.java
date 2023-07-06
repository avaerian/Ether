package org.minerift.ether;

import org.bukkit.plugin.java.JavaPlugin;
import org.minerift.ether.debug.NMSChunkDebugCommand;
import org.minerift.ether.debug.NMSSetBlocksDebugCommand;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.nms.NMS;

import java.util.logging.Level;

public class EtherPlugin extends JavaPlugin {

    private static EtherPlugin INSTANCE = null;


    private boolean isUsingWorldEdit;

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
        this.isUsingWorldEdit = false;


        // Register debug command
        getCommand("nmschunk").setExecutor(new NMSChunkDebugCommand());
        getCommand("nmsblock").setExecutor(new NMSSetBlocksDebugCommand());


        getLogger().log(Level.INFO, "Ether plugin enabled!");
    }

    @Override
    public void onDisable() {

    }

    public static EtherPlugin getInstance() {
        return INSTANCE;
    }

    public boolean isUsingWorldEdit() {
        return isUsingWorldEdit;
    }

    public NMS getNMS() {
        return nms;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }
}
