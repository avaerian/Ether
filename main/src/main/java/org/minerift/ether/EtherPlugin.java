package org.minerift.ether;

import org.bukkit.plugin.java.JavaPlugin;
import org.minerift.ether.debug.NMSBlockScanDebugCommand;
import org.minerift.ether.debug.NMSChunkDebugCommand;
import org.minerift.ether.debug.NMSSetBlocksDebugCommand;
import org.minerift.ether.debug.SchematicDebugCommand;

import java.util.logging.Level;

public class EtherPlugin extends JavaPlugin {

    @Override
    public void onLoad() {

        Ether.onLoad(this);

    }

    @Override
    public void onEnable() {

        Ether.onEnable();

        // Register debug command
        getCommand("nmschunk").setExecutor(new NMSChunkDebugCommand());
        getCommand("nmsblock").setExecutor(new NMSSetBlocksDebugCommand());
        getCommand("blockscan").setExecutor(new NMSBlockScanDebugCommand());
        getCommand("pasteschem").setExecutor(new SchematicDebugCommand());

        getLogger().log(Level.INFO, "Ether plugin enabled!");
    }

    @Override
    public void onDisable() {

        Ether.onDisable();

    }
}
