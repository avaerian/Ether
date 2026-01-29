package org.minerift.ether.schematic;

import org.minerift.ether.EtherPlugin;
import org.minerift.ether.schematic.sponge.SpongeSchematic;
import org.minerift.ether.schematic.sponge.SpongeSchematicPaster;
import org.minerift.ether.schematic.worldedit.WESchematicPaster;
import org.minerift.ether.schematic.sponge.SpongeSchematicCodec;
import org.minerift.ether.schematic.worldedit.WESchematicCodec;
import org.minerift.ether.schematic.worldedit.WorldEditSchematic;

public class SchematicType<S extends Schematic> {

    private final static SchematicType UNSUPPORTED;
    public final static SchematicType<SpongeSchematic> SPONGE;
    public final static SchematicType<WorldEditSchematic> WORLDEDIT;

    static {
        UNSUPPORTED = new SchematicType("None", null, null);
        SPONGE = new SchematicType<>("Sponge", SpongeSchematicCodec.INST, new SpongeSchematicPaster());
        // Initialize only if WorldEdit is supported
        WORLDEDIT = EtherPlugin.getInstance().getServer()
                .getPluginManager()
                .isPluginEnabled("WorldEdit")
                ? new SchematicType<>("WorldEdit", new WESchematicCodec(), new WESchematicPaster())
                : UNSUPPORTED;
    }

    private final String name;
    private final SchematicCodec<S> codec;
    private final SchematicPaster<S> paster;
    private SchematicType(String name, SchematicCodec<S> codec, SchematicPaster<S> paster) {
        this.name = name;
        this.codec = codec;
        this.paster = paster;
    }

    public static SchematicType<?> from(String schemType) {
        return switch (schemType.toUpperCase()) {
            case "WORLDEDIT" -> WORLDEDIT;
            case "DEFAULT", "SPONGE" -> SPONGE;
            default -> throw new IllegalStateException("Unexpected value: " + schemType.toUpperCase());
        };
    }

    public String getName() {
        return name;
    }

    public SchematicCodec<S> codec() {
        if(codec == null) {
            throw new UnsupportedOperationException("Reader unavailable; schematic type was unable to load");
        }
        return codec;
    }

    public SchematicPaster<S> getPaster() {
        if(paster == null) {
            throw new UnsupportedOperationException("Paster unavailable; schematic type was unable to load");
        }
        return paster;
    }

    public <P extends SchematicPaster<S>> P getPaster(Class<P> clazz) {
        return clazz.cast(getPaster());
    }

}
