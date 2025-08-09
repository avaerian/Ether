package org.minerift.ether;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.minerift.ether.debug.*;
import org.minerift.ether.listeners.BlockBreakListener;
import org.minerift.ether.listeners.PlayerJoinQuitListener;

// Represents the Minecraft plugin (handles plugin API stuff here)
public class EtherPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        Ether.onLoad(this);
    }

    @Override
    public void onEnable() {
        Ether.onEnable();

        // Register debug commands
        getCommand("island").setExecutor(new IslandDebugCommand());

        getCommand("nmschunk").setExecutor(new NMSChunkDebugCommand());
        getCommand("nmsblock").setExecutor(new NMSSetBlocksDebugCommand());
        getCommand("blockscan").setExecutor(new NMSBlockScanDebugCommand());
        getCommand("pasteschem").setExecutor(new SchematicDebugCommand());
        getCommand("cfgreload").setExecutor(new ConfigReloadDebugCommand());

        getCommand("testreg").setExecutor(new NMSRegistryDebugCommand());
        getCommand("dbgnbt").setExecutor(new NbtDebugCommand());

        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerJoinQuitListener(), this);
    }

    @Override
    public void onDisable() {
        Ether.onDisable();
    }

    public void disable() {
        Bukkit.getPluginManager().disablePlugin(this);
    }
}
