package org.minerift.ether.config.main;

public class MainConfigPaths {

    //public static final String TILE_HEIGHT = "island.tile.height";
    //public static final String TILE_SIZE_CHUNKS = "island.tile.size";
    //public static final String TILE_ACCESSIBLE_AREA = "island.tile.accessible_area";

    public static final String PERSIST_METHOD = "persist.method";
    public static final String SQL_DIALECT = "persist.sql.dialect";
    public static final String SQL_URL = "persist.sql.url";
    public static final String SQL_USERNAME = "persist.sql.username";
    public static final String SQL_PASSWORD = "persist.sql.password";

    public static final String DEFAULT_SCHEM_TYPE = "schematic.type";

    public static final String PURGE_ISLANDS_OPTION = "island.manage.should_insta_delete_islands";
    public static final String TIME_UNTIL_NEXT_PURGE_SECS = "island.manage.time_until_next_purge_seconds"; // FIXME: need comment for this in yaml to signify seconds time unit
    public static final String PURGED_ISLANDS_THRESHOLD = "island.manage.purged_islands_threshold";
    public static final String MIN_PURGED_ISLANDS = "island.manage.min_purged_islands"; // needs comment; per clear
    public static final String MAX_PURGED_ISLANDS = "island.manage.max_purged_islands"; // needs comment; per clear

    public static final String DIMENSIONS = "island.dimensions";

    private MainConfigPaths() {}
}
