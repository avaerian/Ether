package org.minerift.ether;

import org.bukkit.plugin.java.JavaPlugin;
import org.minerift.ether.debug.NMSBlockScanDebugCommand;
import org.minerift.ether.debug.NMSChunkDebugCommand;
import org.minerift.ether.debug.NMSSetBlocksDebugCommand;
import org.minerift.ether.debug.SchematicDebugCommand;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.work.WorkQueue;

import java.util.logging.Level;

public class EtherPlugin extends JavaPlugin {

    private static EtherPlugin INSTANCE = null;

    private boolean isUsingWorldEdit;

    private WorkQueue workQueue;
    private IslandManager islandManager;

    public static EtherPlugin getInstance() {
        return INSTANCE;
    }

    @Override
    public void onLoad() {

    }

    @Override
    public void onEnable() {

        INSTANCE = this;
        Ether.load(INSTANCE);

        // Load other stuff
        this.isUsingWorldEdit = false; // TODO
        this.workQueue = new WorkQueue();
        workQueue.start();

        this.islandManager = new IslandManager();

        // Register debug command
        getCommand("nmschunk").setExecutor(new NMSChunkDebugCommand());
        getCommand("nmsblock").setExecutor(new NMSSetBlocksDebugCommand());
        getCommand("blockscan").setExecutor(new NMSBlockScanDebugCommand());
        getCommand("pasteschem").setExecutor(new SchematicDebugCommand());

        getLogger().log(Level.INFO, "Ether plugin enabled!");
    }

    @Override
    public void onDisable() {
        workQueue.close();
        workQueue = null;

        // TODO: close ConfigManager
    }

    public boolean isUsingWorldEdit() {
        return isUsingWorldEdit;
    }

    /*
    public NMSAccess getNMS() {
        return nmsAccess;
    }
    */

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public WorkQueue getWorkQueue() {
        return workQueue;
    }
}
