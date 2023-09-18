package org.minerift.ether.config.types;

import org.minerift.ether.config.Config;

public class MainConfig extends Config<MainConfig> {

    public MainConfig() {
        //this.file = new File(getPluginDirectory(), "config.yml");
    }

    @Override
    protected void copyFrom(MainConfig other) {

    }

    @Override
    public ConfigType<MainConfig> getType() {
        return ConfigType.MAIN;
    }
}
