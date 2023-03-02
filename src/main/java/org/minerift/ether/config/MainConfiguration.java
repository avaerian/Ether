package org.minerift.ether.config;

/**
 * Represents the config.yml that is loaded into memory for ease-of-use
 * @author Avaerian
 */

@ConfigurationFile(name = "config.yml")
public class MainConfiguration { // TODO: rename to something more appropriate

    @YamlPath(path = "island.tile.size")
    public static int TILE_SIZE = 200; // default value for now

}
