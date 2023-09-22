package org.minerift.ether.config.types;

import org.minerift.ether.config.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchematicConfig extends Config<SchematicConfig> {

    private List<File> schemFiles;

    public SchematicConfig() {
        this.schemFiles = new ArrayList<>();
    }

    public List<File> getSchematicFiles() {
        return schemFiles;
    }

    @Override
    protected void copyFrom(SchematicConfig other) {
        this.schemFiles = other.schemFiles;
    }

    @Override
    public ConfigType<SchematicConfig> getType() {
        return ConfigType.SCHEM_LIST;
    }
}
