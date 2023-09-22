package org.minerift.ether.config.readers;

import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;
import org.minerift.ether.config.types.SchematicConfig;

import java.io.FileNotFoundException;

public class SchematicConfigReader extends IConfigReader<SchematicConfig> {
    @Override
    public SchematicConfig read(ConfigType<SchematicConfig> type) throws ConfigFileReadException, FileNotFoundException {

        ensureFileExists(type);

        return null;
    }
}
