package org.minerift.ether.config;

/**
 * Represents the config.yml that is loaded into memory for ease-of-use
 * @author Avaerian
 */
@ConfigurationFile(name = "config.yml")
public class MainConfiguration {

    @YamlPath(path = "island.tile.size")
    public final static int TILE_SIZE = 200; // default value for now

    @YamlPath(path = "island.tile.height")
    public final static int TILE_HEIGHT = 90; // default value for now

}
