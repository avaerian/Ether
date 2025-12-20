package org.minerift.ether.config.main;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.source.FileSource;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.schematic.SchematicType;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainConfig extends Config<MainConfig> {

    public static final int CHUNK_SIZE = 16;
    public static final int MIN_TILE_CHUNKS = 3;
    public static final int MIN_TILE_SIZE = MIN_TILE_CHUNKS * CHUNK_SIZE; // 3 chunks * 16 blocks/chunk = 48 blocks

    private int tileLengthChunks;
    private int tileHeight;

    // I plan on adding permissions to this and allowing for different tiers
    private int tileAccessibleAreaBlocks;

    private long inviteInvalidateAfter; // ms time unit

    private Database.Type dbType;
    private SQLDialect sqlDialect;
    private String sqlUrl; // optional, depending on dialect
    private String sqlUsername;
    private String sqlPassword;

    private SchematicType<?> defaultSchemType;

    // Default values for config
    public MainConfig(ConfigRegistry reg, FileSource src) {
        super(reg, src);
        this.tileLengthChunks = 9; // default value for now
        this.tileHeight = 90;
        this.tileAccessibleAreaBlocks = 180; // default value for now; this is subject to change
        this.inviteInvalidateAfter = TimeUnit.MINUTES.toMillis(2);

        this.dbType = Database.Type.SQL;
        this.sqlDialect = SQLDialect.H2;
        this.sqlUrl = "";
        this.sqlUsername = "root";
        this.sqlPassword = "";

        this.defaultSchemType = SchematicType.SPONGE;
    }

    // Getters
    public Database.Type getPersistMethod() {
        return dbType;
    }

    public SQLDialect getSqlDialect() {
        return sqlDialect;
    }

    public String getSqlUrl() {
        return sqlUrl;
    }

    public boolean hasSqlUrl() {
        return sqlUrl != null && !sqlUrl.isBlank();
    }

    public String getSqlUsername() {
        return sqlUsername;
    }

    public String getSqlPassword() {
        return sqlPassword;
    }

    public int getTileLengthChunks() {
        return tileLengthChunks;
    }

    public int getTileLengthBlocks() {
        return tileLengthChunks * CHUNK_SIZE;
    }

    public int getTileHeight() {
        return tileHeight;
    }

    public int getTileAccessibleAreaBlocks() {
        return tileAccessibleAreaBlocks;
    }

    public long getInviteInvalidateAfter() {
        return inviteInvalidateAfter;
    }

    public SchematicType<?> getDefaultSchemType() {
        return defaultSchemType;
    }

    // TODO: review use of setters; move to use Builder pattern once more for immutability
    // Setters
    public void setPersistMethod(Database.Type dbType) {
        this.dbType = dbType;
    }

    public void setSqlDialect(SQLDialect sqlDialect) {
        this.sqlDialect = sqlDialect;
    }

    public void setSqlUrl(String sqlUrl) {
        this.sqlUrl = sqlUrl;
    }

    public void setSqlUsername(String sqlUsername) {
        this.sqlUsername = sqlUsername;
    }

    public void setSqlPassword(String sqlPassword) {
        this.sqlPassword = sqlPassword;
    }

    public void setInviteInvalidateAfter(long ms) {
        this.inviteInvalidateAfter = ms;
    }

    public void setTileLengthChunks(int tileLengthChunks) {
        this.tileLengthChunks = tileLengthChunks;
    }

    public void setDefaultSchemType(SchematicType<?> type) {
        this.defaultSchemType = type;
    }

    /*
    @Deprecated(forRemoval = true)
    // Don't use this function. The variables are all wrong so it won't work properly.
    public void setTileSizeBlocks(int tileSize) {
        // Round the tile size down to chunks (multiples of 16)
        final int tileSizeActual = tileSize - (tileSize % CHUNK_SIZE);
        checkArgument(tileSizeActual >= MIN_TILE_SIZE, String.format("Tile size (%d -> %d) cannot be below min size %d!", tileSize, tileSizeActual, MIN_TILE_SIZE));

        this.tileLengthChunks = tileSizeActual;

        // Update accessible region if bigger than new tile size
        if(tileAccessibleArea > tileSizeActual) {
            tileAccessibleArea = tileSizeActual;
        }

        setChanged(true);
    }*/

    public void setTileHeight(int tileHeight) {
        this.tileHeight = tileHeight;
    }

    public void setTileAccessibleAreaBlocks(int tileAccessibleAreaBlocks) {
        this.tileAccessibleAreaBlocks = tileAccessibleAreaBlocks;
    }

    @Override
    protected void copyFrom(MainConfig o) {
        if(!o.equals(this)) {
            this.tileLengthChunks = o.tileLengthChunks;
            this.tileHeight = o.tileHeight;
            this.tileAccessibleAreaBlocks = o.tileAccessibleAreaBlocks;
            this.inviteInvalidateAfter = o.inviteInvalidateAfter;

            this.dbType = o.dbType;
            this.sqlDialect = o.sqlDialect;
            this.sqlUrl = o.sqlUrl;
            this.sqlUsername = o.sqlUsername;
            this.sqlPassword = o.sqlPassword;

            this.defaultSchemType = o.defaultSchemType;

        }
    }

    @Override // TODO: update to include new fields
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MainConfig that = (MainConfig) o;
        return tileLengthChunks == that.tileLengthChunks
                && tileHeight == that.tileHeight
                && tileAccessibleAreaBlocks == that.tileAccessibleAreaBlocks
                && inviteInvalidateAfter == that.inviteInvalidateAfter;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tileLengthChunks, tileHeight, tileAccessibleAreaBlocks, inviteInvalidateAfter);
    }

    @Override
    public ConfigType<MainConfig, FileSource> getType() {
        return ConfigType.MAIN;
    }
}
