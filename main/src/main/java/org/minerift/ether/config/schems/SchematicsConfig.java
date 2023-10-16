package org.minerift.ether.config.schems;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigType;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SchematicsConfig extends Config<SchematicsConfig> {

    private List<File> schemFiles;

    public SchematicsConfig() {
        this.schemFiles = new ArrayList<>();
    }

    public List<File> getSchemFiles() {
        return schemFiles;
    }

    public void setSchemFiles(List<File> schemFiles) {
        this.schemFiles = schemFiles;
    }

    @Override
    protected void copyFrom(SchematicsConfig other) {
        this.schemFiles = other.schemFiles;
    }

    @Override
    public ConfigType<SchematicsConfig> getType() {
        return ConfigType.SCHEM_LIST;
    }
}
