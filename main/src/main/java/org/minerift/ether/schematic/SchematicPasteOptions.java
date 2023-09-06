package org.minerift.ether.schematic;

import org.minerift.ether.util.math.Vec3i;

public class SchematicPasteOptions {

    /**
     * Default SchematicPasteOptions with no changes.
     * Everything is defaulted to false or 0.
     */
    public static final SchematicPasteOptions EMPTY_DEFAULT;

    /**
     * Preferred default SchematicPasteOptions.
     * Will copy biomes and entities with zero offset and no ignored air blocks.
     */
    public static final SchematicPasteOptions DEFAULT;

    static {
        EMPTY_DEFAULT = SchematicPasteOptions.builder().build();
        DEFAULT = SchematicPasteOptions.builder()
                .copyBiomes(true)
                .copyEntities(true)
                .build();
    }

    public final boolean copyBiomes;
    public final boolean copyEntities;
    public final boolean ignoreAirBlocks;
    public final Vec3i offset;

    private SchematicPasteOptions(SchematicPasteOptions.Builder builder) {
        this.copyBiomes = builder.copyBiomes;
        this.copyEntities = builder.copyEntities;
        this.ignoreAirBlocks = builder.ignoreAirBlocks;
        this.offset = builder.offset;
    }

    public static SchematicPasteOptions.Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private boolean copyBiomes;
        private boolean copyEntities;
        private boolean ignoreAirBlocks;
        private Vec3i offset;

        private Builder() {
            this.copyBiomes = false;
            this.copyEntities = false;
            this.ignoreAirBlocks = false;
            this.offset = Vec3i.ZERO;
        }

        public Builder copyBiomes(boolean val) {
            this.copyBiomes = val;
            return this;
        }

        public Builder copyEntities(boolean val) {
            this.copyEntities = val;
            return this;
        }

        public Builder ignoreAirBlocks(boolean val) {
            this.ignoreAirBlocks = val;
            return this;
        }

        public Builder setOffset(Vec3i val) {
            this.offset = val;
            return this;
        }

        public SchematicPasteOptions build() {
            return new SchematicPasteOptions(this);
        }
    }

}
