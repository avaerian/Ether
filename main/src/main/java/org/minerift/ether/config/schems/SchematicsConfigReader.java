package org.minerift.ether.config.schems;

import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.IConfigReader;

import java.io.File;

public class SchematicsConfigReader extends IConfigReader<SchematicsConfig> {
    @Override
    protected SchematicsConfig readIt(File file) throws ConfigFileReadException {
        return null;
    }
}
