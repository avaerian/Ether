package org.minerift.ether.config.schems;

import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.IConfigReader;

import java.io.File;

public class SchematicConfigReader extends IConfigReader<SchematicConfig> {
    @Override
    protected SchematicConfig readIt(File file) throws ConfigFileReadException {

        return null;
    }
}
