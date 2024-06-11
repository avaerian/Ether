package org.minerift.ether.config.islandspecs;

import org.minerift.ether.config.IConfigReader;
import org.minerift.ether.config.exceptions.ConfigFileReadException;

import java.io.File;

public class IslandSpecsReader extends IConfigReader<IslandSpecsConfig> {
    @Override
    protected IslandSpecsConfig readIt(File file) throws ConfigFileReadException {
        return null;
    }
}
