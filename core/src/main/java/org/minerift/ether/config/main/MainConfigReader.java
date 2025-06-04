package org.minerift.ether.config.main;

import org.minerift.ether.config.YamlConfigView;
import org.minerift.ether.config.IConfigReader;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.nusql.SQLDialect;
import org.minerift.ether.util.UnreachableException;

import java.io.File;
import java.io.IOException;

import static org.minerift.ether.config.main.MainConfigPaths.*;

public class MainConfigReader extends IConfigReader<MainConfig> {

    @Override
    protected MainConfig readIt(File file) throws ConfigFileReadException {

        try {
            YamlConfigView view = YamlConfigView.from(file);
            MainConfig config = new MainConfig();

            config.setTileHeight(         view.get(Integer.class, TILE_HEIGHT_PATH).orElseThrow(() -> new ConfigFileReadException("Failed to read tile height!")));
            config.setTileLengthChunks(     view.get(Integer.class, TILE_SIZE_CHUNKS_PATH).orElseThrow(() -> new ConfigFileReadException("Failed to read tile size!")));
            config.setTileAccessibleAreaBlocks( view.get(Integer.class, TILE_ACCESSIBLE_AREA_PATH).orElseThrow(() -> new ConfigFileReadException("Failed to read tile accessible area!")));

            // TODO: this could use some review for better exception explanations, but this is fine for now
            Database.Type dbType = view.get(String.class, PERSIST_METHOD)
                    .map(Database.Type::valueOfSilent)
                    .orElseThrow(() -> new ConfigFileReadException("Failed to read database type (persistence method)"));

            config.setPersistMethod(dbType);
            switch(dbType) {
                case SQL -> {
                    SQLDialect dialect = view.get(String.class, SQL_DIALECT)
                            .map(SQLDialect::valueOfSilent)
                            .orElseThrow(() -> new ConfigFileReadException("Failed to read SQL dialect"));
                    config.setSqlDialect(dialect);

                    config.setSqlUrl(view.get(String.class, SQL_URL).orElse("")); // NOTE: PostgreSQL and MySQL should use this, otherwise this can be excluded
                    config.setSqlUsername(view.get(String.class, SQL_USERNAME).orElseThrow(() -> new ConfigFileReadException("Failed to read SQL username")));
                    config.setSqlPassword(view.get(String.class, SQL_PASSWORD).orElseThrow(() -> new ConfigFileReadException("Failed to read SQL password")));
                }
                case BIN -> throw new UnsupportedOperationException("Unimplemented");
                case null -> throw new UnreachableException("This should be unreachable");
                default -> throw new UnsupportedOperationException(dbType + " is not being handled yet");
            }


            config.setChanged(false);

            return config;
        } catch (IllegalArgumentException ex) { // Thrown when failing to set values
            throw new ConfigFileReadException(ex);
        } catch (IOException ex) {
            throw new ConfigFileReadException(ex);
        }
    }
}
