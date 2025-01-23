package org.minerift.ether.config.main;

import org.minerift.ether.config.YamlConfigView;
import org.minerift.ether.config.IConfigWriter;
import org.minerift.ether.config.exceptions.ConfigFileWriteException;
import org.minerift.ether.util.UnreachableException;

import java.io.File;
import java.io.IOException;

import static org.minerift.ether.config.main.MainConfigPaths.*;

public class MainConfigWriter extends IConfigWriter<MainConfig> {

    @Override
    protected void writeIt(MainConfig config, File file) throws ConfigFileWriteException {
        try {
            final YamlConfigView view = YamlConfigView.from(file);

            // General island settings
            view.set(TILE_HEIGHT_PATH,          config.getTileHeight());
            view.set(TILE_SIZE_CHUNKS_PATH,     config.getTileLengthChunks());
            view.set(TILE_ACCESSIBLE_AREA_PATH, config.getTileAccessibleAreaBlocks());

            // Database settings
            view.set(PERSIST_METHOD, config.getPersistMethod());
            switch(config.getPersistMethod()) {
                case SQL -> {
                    view.set(SQL_DIALECT, config.getSqlDialect().name());
                    view.set(SQL_URL, config.hasSqlUrl() ? config.getSqlUrl() : "");
                    view.set(SQL_USERNAME, config.getSqlUsername());
                    view.set(SQL_PASSWORD, config.getSqlPassword());
                }
                case BIN -> throw new UnsupportedOperationException("Unimplemented");
                case null -> throw new UnreachableException("This should be unreachable");
                default -> throw new UnsupportedOperationException(config.getPersistMethod() + " is not handled yet");
            }

            view.save(file);
        } catch (IOException ex) {
            throw new ConfigFileWriteException(ex);
        }
    }
}
