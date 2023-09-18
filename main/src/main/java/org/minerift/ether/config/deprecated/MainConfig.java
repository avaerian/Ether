package org.minerift.ether.config.deprecated;

/**
 * Represents the config.yml that is loaded into memory for ease-of-use
 * @author Avaerian
 */
@Deprecated
@DeprecatedConfigurationFile(name = "config.yml")
public class MainConfig {

    @DeprecatedYamlPath(path = "island.tile.size")
    public final static int TILE_SIZE = 200; // default value for now

    @DeprecatedYamlPath(path = "island.tile.height")
    public final static int TILE_HEIGHT = 90;

    // I plan on adding permissions to this and allowing for different tiers
    @DeprecatedYamlPath(path = "island.tile.height")
    public final static int TILE_ACCESSIBLE_AREA = 150; // default value for now; this is subject to change

}
