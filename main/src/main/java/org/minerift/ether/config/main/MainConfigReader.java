package org.minerift.ether.config.main;

import org.minerift.ether.config.YamlConfigView;
import org.minerift.ether.config.IConfigReader;
import org.minerift.ether.config.exceptions.ConfigFileReadException;

import java.io.File;
import java.io.IOException;

import static org.minerift.ether.config.main.MainConfigPaths.*;

public class MainConfigReader extends IConfigReader<MainConfig> {

    @Override
    protected MainConfig readIt(File file) throws ConfigFileReadException {

        try {
            YamlConfigView view = YamlConfigView.from(file);
            MainConfig config = new MainConfig();

            config.setTileHeight(         view.get(Integer.class, TILE_HEIGHT_PATH).orElseThrow(() -> new ConfigFileReadException("Cannot read tile height!")));
            config.setTileSize(           view.get(Integer.class, TILE_SIZE_PATH).orElseThrow(() -> new ConfigFileReadException("Cannot read tile size!")));
            config.setTileAccessibleArea( view.get(Integer.class, TILE_ACCESSIBLE_AREA_PATH).orElseThrow(() -> new ConfigFileReadException("Cannot read tile accessible area!")));
            config.setChanged(false);

            return config;
        } catch (IllegalArgumentException ex) { // Thrown when failing to set values
            throw new ConfigFileReadException(ex);
        } catch (IOException ex) {
            throw (ConfigFileReadException) ex;
        }
    }
}
