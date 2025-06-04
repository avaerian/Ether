package org.minerift.ether.schematic;

import org.minerift.ether.Ether;
import org.minerift.ether.schematic.sponge.DeprecatedSpongeSchematicPaster;
import org.minerift.ether.schematic.sponge.SpongeSchematicPaster;
import org.minerift.ether.schematic.worldedit.WESchematicPaster;
import org.minerift.ether.schematic.sponge.reader.SpongeSchematicReader;
import org.minerift.ether.schematic.worldedit.WESchematicReader;

public class SchematicType {

    private final static SchematicType UNSUPPORTED;
    public final static SchematicType SPONGE;
    public final static SchematicType WORLDEDIT;

    static {
        //UNSUPPORTED = new SchematicType(null, null);
        UNSUPPORTED = null;
        SPONGE = new SchematicType(new SpongeSchematicReader(), new SpongeSchematicPaster());
        // Initialize only if WorldEdit is supported
        WORLDEDIT = Ether.isUsingWorldEdit()
                ? new SchematicType(new WESchematicReader(), new WESchematicPaster())
                : UNSUPPORTED;
    }

    private final SchematicReader<? extends Schematic> reader;
    private final SchematicPaster<? extends Schematic> paster;
    private SchematicType(SchematicReader<? extends Schematic> reader, SchematicPaster<? extends Schematic> paster) {
        this.reader = reader;
        this.paster = paster;
    }

    public boolean isSupported() {
        return this != UNSUPPORTED;
    }

    public SchematicReader<? extends Schematic> getReader() {
        if(!isSupported()) {
            throw new UnsupportedOperationException("Reader unavailable because schematic type was unable to load!");
        }
        return reader;
    }

    public SchematicPaster<? extends Schematic> getPaster() {
        if(!isSupported()) {
            throw new UnsupportedOperationException("Paster unavailable because schematic type was unable to load!");
        }
        return paster;
    }

    public <P extends SchematicPaster<? extends Schematic>> P getPaster(Class<P> clazz) {
        return clazz.cast(getPaster());
    }

}
