package org.minerift.ether.schematic.types;

import org.minerift.ether.EtherPlugin;
import org.minerift.ether.schematic.pasters.ISchematicPaster;
import org.minerift.ether.schematic.pasters.SpongeSchematicPaster;
import org.minerift.ether.schematic.pasters.WESchematicPaster;
import org.minerift.ether.schematic.readers.ISchematicReader;
import org.minerift.ether.schematic.readers.sponge.SpongeSchematicReader;
import org.minerift.ether.schematic.readers.worldedit.WESchematicReader;

public class SchematicType {

    private final static SchematicType UNSUPPORTED;
    public final static SchematicType SPONGE;
    public final static SchematicType WORLDEDIT;

    static {
        UNSUPPORTED = new SchematicType(null, null);
        SPONGE = new SchematicType(new SpongeSchematicReader(), new SpongeSchematicPaster());
        // Initialize only if WorldEdit is supported
        WORLDEDIT = EtherPlugin.getInstance().isUsingWorldEdit()
                ? new SchematicType(new WESchematicReader(), new WESchematicPaster())
                : UNSUPPORTED;
    }

    private final ISchematicReader<? extends Schematic> reader;
    private final ISchematicPaster<? extends Schematic> paster;
    private SchematicType(ISchematicReader<? extends Schematic> reader, ISchematicPaster<? extends Schematic> paster) {
        this.reader = reader;
        this.paster = paster;
    }

    public boolean isSupported() {
        return this != UNSUPPORTED;
    }

    public ISchematicReader<? extends Schematic> getReader() {
        if(!isSupported()) {
            throw new UnsupportedOperationException("Reader unavailable because schematic type was unable to load!");
        }
        return reader;
    }

    public ISchematicPaster<? extends Schematic> getPaster() {
        if(!isSupported()) {
            throw new UnsupportedOperationException("Paster unavailable because schematic type was unable to load!");
        }
        return paster;
    }

    public <P extends ISchematicPaster<? extends Schematic>> P getPaster(Class<P> clazz) {
        return clazz.cast(getPaster());
    }

}
