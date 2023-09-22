package org.minerift.ether.config.types;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.annotations.YamlComment;
import org.minerift.ether.config.annotations.YamlPath;

public class MainConfig extends Config<MainConfig> {

    @YamlComment(line = "Adjusts the size of each tile.")
    @YamlPath(path = "island.tile.size")
    private int tileSize;

    @YamlComment(line = "Adjusts the height at which the island is created.")
    @YamlPath(path = "island.tile.height")
    private int tileHeight;

    @YamlComment(line = "Adjusts the size of the interactable area on each island.")
    @YamlPath(path = "island.tile.accessible_area")
    // I plan on adding permissions to this and allowing for different tiers
    private int tileAccessibleArea;

    public MainConfig() {
        //this.file = new File(getPluginDirectory(), "config.yml");
        this.tileSize = 200; // default value for now
        this.tileHeight = 90;
        this.tileAccessibleArea = 150; // default value for now; this is subject to change
    }

    @Override
    protected void copyFrom(MainConfig other) {
        this.tileSize = other.tileSize;
        this.tileHeight = other.tileHeight;
        this.tileAccessibleArea = other.tileAccessibleArea;
    }

    @Override
    public ConfigType<MainConfig> getType() {
        return ConfigType.MAIN;
    }

    public int getTileSize() {
        return tileSize;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getTileAccessibleArea() {
        return tileAccessibleArea;
    }

    // TODO: setters
    // Some setters will need additional logging to let the
    // user know that an important/critical setting was updated
}
