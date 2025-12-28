package org.minerift.ether;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.minerift.ether.debug.*;
import org.minerift.ether.listeners.BlockBreakListener;
import org.minerift.ether.listeners.PlayerJoinQuitListener;

import java.util.logging.Level;
import java.util.logging.Logger;

// Represents the Minecraft plugin (handles plugin API stuff here)
public class EtherPlugin extends JavaPlugin {

    private static EtherPlugin INST;

    @Deprecated
    public static EtherPlugin getInstance() {
        return INST;
    }

    @Override
    public void onLoad() {
        INST = this;
    }

    @Override
    public void onEnable() {
        final Logger logger = getLogger();
        final Ether.InitResult init;
        try {
            init = Ether.from(getDataFolder(), logger);
        } catch (EtherLoadException e) {
            logger.log(Level.SEVERE, "Something went wrong", e);
            disable();
            return;
        }
        Ether.INST = init.ether;

        // Register debug commands
        getCommand("island").setExecutor(new IslandDebugCommand());

        getCommand("nmschunk").setExecutor(new NMSChunkDebugCommand());
        getCommand("nmsblock").setExecutor(new NMSSetBlocksDebugCommand());
        getCommand("blockscan").setExecutor(new NMSBlockScanDebugCommand());
        getCommand("pasteschem").setExecutor(new SchematicDebugCommand());
        getCommand("cfgreload").setExecutor(new ConfigReloadDebugCommand());

        getCommand("testreg").setExecutor(new NMSRegistryDebugCommand());
        getCommand("dbgnbt").setExecutor(new NbtDebugCommand());
        getCommand("dbgbvt").setExecutor(new TransformDebugCommand());
        getCommand("dbgbb").setExecutor(new BoundingBoxDebugCommand());

        final PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new BlockBreakListener(), this);
        pm.registerEvents(new PlayerJoinQuitListener(), this);
    }

    @Override
    public void onDisable() {
        Ether.inst().close();
        INST = null;
    }

    public void disable() {
        Bukkit.getPluginManager().disablePlugin(this);
    }
}
