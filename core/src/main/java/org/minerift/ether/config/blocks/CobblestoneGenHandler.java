package org.minerift.ether.config.blocks;

public enum CobblestoneGenHandler {
    /**
     * Disabled; not using our cobblestone generator system.
     */
    NONE,

    /**
     * Each dimension handles its own CobblestoneGen settings.
     */
    PER_DIMENSION,

    /**
     * A master CobblestoneGen settings to be used globally.
     */
    GLOBAL,
}
