package org.minerift.ether;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class EtherPlugin extends JavaPlugin {

    @Override
    public void onLoad() {
        Ether.onLoad(this);
    }

    @Override
    public void onEnable() {
        Ether.onEnable();
    }

    @Override
    public void onDisable() {
        Ether.onDisable();
    }

    public void disable() {
        Bukkit.getPluginManager().disablePlugin(this);
    }
}
