package org.minerift.ether;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class EtherPlugin extends JavaPlugin {

    private static EtherPlugin INSTANCE = null;

    @Override
    public void onLoad() {
        // TODO: look into when this method is called vs. onEnable()
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        this.getLogger().log(Level.INFO, "Ether plugin enabled!");
    }

    @Override
    public void onDisable() {

    }
}
