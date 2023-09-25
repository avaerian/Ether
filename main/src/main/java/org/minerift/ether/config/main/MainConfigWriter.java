package org.minerift.ether.config.main;

import org.minerift.ether.config.YamlConfigView;
import org.minerift.ether.config.IConfigWriter;
import org.minerift.ether.config.exceptions.ConfigFileWriteException;

import java.io.File;
import java.io.IOException;

import static org.minerift.ether.config.main.MainConfigPaths.*;

public class MainConfigWriter extends IConfigWriter<MainConfig> {

    @Override
    protected void writeIt(MainConfig config, File file) throws ConfigFileWriteException {
        try {
            final YamlConfigView view = YamlConfigView.from(file);

            view.set(TILE_HEIGHT_PATH,          config.getTileHeight());
            view.set(TILE_SIZE_PATH,            config.getTileSize());
            view.set(TILE_ACCESSIBLE_AREA_PATH, config.getTileAccessibleArea());

            view.save(file);
        } catch (IOException ex) {
            throw (ConfigFileWriteException) ex;
        }
    }
}
