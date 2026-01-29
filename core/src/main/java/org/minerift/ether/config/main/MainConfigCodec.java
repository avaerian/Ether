package org.minerift.ether.config.main;

import org.bukkit.NamespacedKey;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigCodec;
import org.minerift.ether.config.ConfigReadException;
import org.minerift.ether.config.YamlConfigView;
import org.minerift.ether.config.ConfigWriteException;
import org.minerift.ether.config.source.FileSource;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.island.PurgeIslandsOption;
import org.minerift.ether.schematic.SchematicType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static org.minerift.ether.config.main.MainConfigPaths.*;

public class MainConfigCodec extends ConfigCodec<MainConfig, FileSource> {

    public static final Logger LOGGER = LoggerFactory.getLogger(MainConfigCodec.class);

    public static final MainConfigCodec INST = new MainConfigCodec();

    @Override
    public void readIt(MainConfig cfg, FileSource src) throws ConfigReadException {
        try {
            YamlConfigView view = YamlConfigView.from(src.getFile());

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

            cfg.setDefaultSchemType( view.get(String.class, DEFAULT_SCHEM_TYPE)
                    .map(SchematicType::from)
                    .orElseThrow(() -> new ConfigReadException("Invalid schematic type (Worldedit, Sponge, Default (Sponge))")));

            // island purge options
            cfg.setPurgeIslandsOption( view.get(String.class, PURGE_ISLANDS_OPTION)
                    .map((str) -> PurgeIslandsOption.valueOf(str.toUpperCase()))
                    .orElseThrow(() -> new ConfigReadException("Failed to read purge islands option (threshold, queued, instant")));
            switch (cfg.purgeIslandsOption()) {
                case QUEUED -> cfg.setTimeUntilNextIslandPurgeSecs( view.get(Integer.class, TIME_UNTIL_NEXT_PURGE_SECS)
                        .orElseThrow(() -> new ConfigReadException("Failed to read time until next purge")));
                case THRESHOLD -> cfg.setPurgedIslandsThreshold( view.get(Integer.class, PURGED_ISLANDS_THRESHOLD)
                        .orElseThrow(() -> new ConfigReadException("Failed to read purged islands threshold")));
            }

            // dimensions
            Map<String, Dimension> dimensions = new HashMap<>();
            YamlConfigView dimSection = view.getSectionView(DIMENSIONS).orElseThrow(() -> new ConfigReadException("Failed to read dimensions"));
            Set<String> names = dimSection.getBukkitView().getKeys(false);
            for(String name : names) {
                YamlConfigView dimView = dimSection.getSectionView(name)
                        .orElseThrow(() -> new ConfigReadException(format("Failed to read name '%s' under %s", name, DIMENSIONS)));
                // TODO: verify dimension resource location
                String dimResLoc = dimView.get(String.class, "resourceLoc")
                        .orElseThrow(() -> new ConfigReadException("Failed to read dimension resource location for "));
                int dimIslandHeight = dimView.get(Integer.class, "islandSpawnY")
                        .orElseThrow(() -> new ConfigReadException("Failed to read dimension islandSpawnY for name " + name));
                int dimTileLenChunks = dimView.get(Integer.class, "tileLenChunks")
                        .orElseThrow(() -> new ConfigReadException("Failed to read dimension tileLenChunks for name " + name));
                int dimTileAccessibleLenBlocks = dimView.get(Integer.class, "tileAccessibleLenBlocks")
                                .orElseThrow(() -> new ConfigReadException("Failed to read tileAccessibleLenBlocks for name " + name));
                final Dimension dim = new Dimension(name,
                        new NamespacedKey(
                                dimResLoc.substring(0, dimResLoc.indexOf(':')),
                                dimResLoc.substring(dimResLoc.indexOf(':') + 1)),
                        dimIslandHeight, dimTileLenChunks, dimTileAccessibleLenBlocks);
                dimensions.put(name, dim);
                LOGGER.debug("Loaded dimension: {}", dim);
            }

            cfg.setDimensions(dimensions);

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

            view.set(DEFAULT_SCHEM_TYPE, cfg.getDefaultSchemType().getName());

            // FIXME: comments needed for the additional properties
            view.set(PURGE_ISLANDS_OPTION, cfg.purgeIslandsOption().name());
            switch (cfg.purgeIslandsOption()) {
                case QUEUED -> view.set(TIME_UNTIL_NEXT_PURGE_SECS, cfg.getTimeUntilNextIslandPurgeSecs());
                case THRESHOLD -> view.set(PURGED_ISLANDS_THRESHOLD, cfg.getPurgedIslandsThreshold());
            }

            for(Dimension dim : cfg.getDimensions().values()) {
                view.set(format("%s.%s.resourceLoc", DIMENSIONS, dim.getName()), dim.getNamespacedKey().asString());
                view.set(format("%s.%s.islandSpawnY", DIMENSIONS, dim.getName()), dim.getIslandSpawnY());
                view.set(format("%s.%s.tileLenChunks", DIMENSIONS, dim.getName()), dim.getTileLenChunks());
                view.set(format("%s.%s.tileAccessibleLenBlocks", DIMENSIONS, dim.getName()), dim.getTileAccessibleLenBlocks());
            }

            view.save(src.getFile());
        } catch (IOException ex) {
            throw new ConfigWriteException(ex);
        }
    }
}
