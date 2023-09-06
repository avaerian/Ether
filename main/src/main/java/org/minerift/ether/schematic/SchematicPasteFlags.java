package org.minerift.ether.schematic;

@Deprecated
public class SchematicPasteFlags {

    public static final byte IGNORE_AIR_BLOCKS = 1;
    public static final byte WITH_BIOMES = 2;
    public static final byte WITH_ENTITIES = 4;

    public static SchematicPasteFlags of(int flags) {
        return new SchematicPasteFlags(flags);
    }

    public static SchematicPasteFlags of(int flag, int ... flags) {
        for(int f : flags) {
            flag |= f;
        }
        return new SchematicPasteFlags(flag);
    }

    private final int flags;
    private SchematicPasteFlags(int flags) {
        this.flags = flags;
    }

    public void has(int flag, Runnable callback) {
        if(has(flag)) {
            callback.run();
        }
    }

    public boolean has(int flag) {
        return (flags & flag) != 0;
    }
}
