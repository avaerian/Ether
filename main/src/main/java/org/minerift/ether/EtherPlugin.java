package org.minerift.ether;

import org.bukkit.plugin.java.JavaPlugin;
import org.minerift.ether.debug.NMSChunkDebugCommand;
import org.minerift.ether.debug.NMSSetBlocksDebugCommand;
import org.minerift.ether.debug.SchematicDebugCommand;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.work.WorkQueue;

import java.util.logging.Level;

public class EtherPlugin extends JavaPlugin {

    private static EtherPlugin INSTANCE = null;


    private boolean isUsingWorldEdit;

    private NMSAccess nmsAccess;
    private WorkQueue workQueue;

    private IslandManager islandManager;

    @Override
    public void onLoad() {
        // TODO: look into when this method is called vs. onEnable()
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.nmsAccess = new NMSAccess();
        this.workQueue = new WorkQueue();
        workQueue.start();

        this.islandManager = new IslandManager();
        this.isUsingWorldEdit = false;


        // Register debug command
        getCommand("nmschunk").setExecutor(new NMSChunkDebugCommand());
        getCommand("nmsblock").setExecutor(new NMSSetBlocksDebugCommand());
        getCommand("pasteschem").setExecutor(new SchematicDebugCommand());


        getLogger().log(Level.INFO, "Ether plugin enabled!");
    }

    @Override
    public void onDisable() {
        workQueue.close();
        workQueue = null;
    }

    public static EtherPlugin getInstance() {
        return INSTANCE;
    }

    public boolean isUsingWorldEdit() {
        return isUsingWorldEdit;
    }

    public NMSAccess getNMS() {
        return nmsAccess;
    }

    public IslandManager getIslandManager() {
        return islandManager;
    }

    public WorkQueue getWorkQueue() {
        return workQueue;
    }
}
