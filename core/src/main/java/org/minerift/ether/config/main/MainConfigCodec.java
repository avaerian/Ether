package org.minerift.ether.config.main;

import org.minerift.ether.config.ConfigCodec;
import org.minerift.ether.config.ConfigReadException;
import org.minerift.ether.config.YamlConfigView;
import org.minerift.ether.config.ConfigWriteException;
import org.minerift.ether.config.source.FileSource;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.sql.SQLDialect;

import java.io.IOException;
import java.nio.channels.FileChannel;

import static java.nio.file.StandardOpenOption.WRITE;
import static org.minerift.ether.config.main.MainConfigPaths.*;

public class MainConfigCodec extends ConfigCodec<MainConfig, FileSource> {

    public static final MainConfigCodec INST = new MainConfigCodec();

    @Override
    public void readIt(MainConfig cfg, FileSource src) throws ConfigReadException {
        try {
            YamlConfigView view = YamlConfigView.from(src.getFile());

            cfg.setTileHeight(         view.get(Integer.class, TILE_HEIGHT_PATH).orElseThrow(() -> new ConfigReadException("Failed to read tile height")));
            cfg.setTileLengthChunks(     view.get(Integer.class, TILE_SIZE_CHUNKS_PATH).orElseThrow(() -> new ConfigReadException("Failed to read tile size")));
            cfg.setTileAccessibleAreaBlocks( view.get(Integer.class, TILE_ACCESSIBLE_AREA_PATH).orElseThrow(() -> new ConfigReadException("Failed to read tile accessible area")));

            // TODO: this could use some review for better exception explanations, but this is fine for now
            Database.Type dbType = view.get(String.class, PERSIST_METHOD)
                    .map(Database.Type::valueOfSilent)
                    .orElseThrow(() -> new ConfigReadException("Failed to read database type (persistence method)"));

            cfg.setPersistMethod(dbType);
            switch(dbType) {
                case SQL -> {
                    SQLDialect dialect = view.get(String.class, SQL_DIALECT)
                            .map(SQLDialect::valueOfSilent)
                            .orElseThrow(() -> new ConfigReadException("Failed to read SQL dialect"));
                    cfg.setSqlDialect(dialect);

                    cfg.setSqlUrl(view.get(String.class, SQL_URL).orElse("")); // NOTE: PostgreSQL and MySQL should use this, otherwise this can be excluded
                    cfg.setSqlUsername(view.get(String.class, SQL_USERNAME).orElseThrow(() -> new ConfigReadException("Failed to read SQL username")));
                    cfg.setSqlPassword(view.get(String.class, SQL_PASSWORD).orElseThrow(() -> new ConfigReadException("Failed to read SQL password")));
                }
                case BIN -> throw new UnsupportedOperationException("unimplemented");
                default -> throw new UnsupportedOperationException(dbType + " has no impl right now");
            }


        } catch (IllegalArgumentException ex) { // Thrown when failing to set values
            throw new ConfigReadException(ex);
        } catch (IOException ex) {
            throw new ConfigReadException(ex);
        }
    }

    @Override
    public void writeIt(MainConfig cfg, FileSource src) throws ConfigWriteException {
        try {
            //FileChannel file = src.openFileChannel(WRITE);
            final YamlConfigView view = YamlConfigView.from(src.getFile()); // TODO: viewOrDefault method for getting a resource as a default format or just setting the values

            // General island settings
            view.set(TILE_HEIGHT_PATH,          cfg.getTileHeight());
            view.set(TILE_SIZE_CHUNKS_PATH,     cfg.getTileLengthChunks());
            view.set(TILE_ACCESSIBLE_AREA_PATH, cfg.getTileAccessibleAreaBlocks());

            // Database settings
            view.set(PERSIST_METHOD, cfg.getPersistMethod().name());
            switch(cfg.getPersistMethod()) {
                case SQL -> {
                    view.set(SQL_DIALECT, cfg.getSqlDialect().name());
                    view.set(SQL_URL, cfg.hasSqlUrl() ? cfg.getSqlUrl() : "");
                    view.set(SQL_USERNAME, cfg.getSqlUsername());
                    view.set(SQL_PASSWORD, cfg.getSqlPassword());
                }
                case BIN -> throw new UnsupportedOperationException("Unimplemented");
                default -> throw new UnsupportedOperationException(cfg.getPersistMethod() + " has no impl right now");
            }

            view.save(src.getFile());
        } catch (IOException ex) {
            throw new ConfigWriteException(ex);
        }
    }
}
