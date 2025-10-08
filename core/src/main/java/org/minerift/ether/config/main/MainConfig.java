package org.minerift.ether.config.main;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.schematic.SchematicType;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainConfig extends Config<MainConfig> {

    public static final MainConfigCodec CODEC = MainConfigCodec.CODEC;
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
    public MainConfig() {
        setTileLengthChunks(9); // default value for now
        setTileHeight(90);
        setTileAccessibleAreaBlocks(180); // default value for now; this is subject to change
        setInviteInvalidateAfter(TimeUnit.MINUTES.toMillis(2));

        setPersistMethod(Database.Type.SQL);
        setSqlDialect(SQLDialect.H2);
        setSqlUrl("");
        setSqlUsername("root");
        setSqlPassword("");

        setDefaultSchemType(SchematicType.SPONGE);

        setChanged(false);
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

    // Setters
    public void setPersistMethod(Database.Type dbType) {
        this.dbType = dbType;
    }

    public void setSqlDialect(SQLDialect sqlDialect) {
        this.sqlDialect = sqlDialect;
        setChanged(true);
    }

    public void setSqlUrl(String sqlUrl) {
        this.sqlUrl = sqlUrl;
        setChanged(true);
    }

    public void setSqlUsername(String sqlUsername) {
        this.sqlUsername = sqlUsername;
        setChanged(true);
    }

    public void setSqlPassword(String sqlPassword) {
        this.sqlPassword = sqlPassword;
        setChanged(true);
    }

    public void setInviteInvalidateAfter(long ms) {
        this.inviteInvalidateAfter = ms;
        setChanged(true);
    }

    public void setTileLengthChunks(int tileLengthChunks) {
        this.tileLengthChunks = tileLengthChunks;
        setChanged(true);
    }

    public void setDefaultSchemType(SchematicType<?> type) {
        this.defaultSchemType = type;
        setChanged(true);
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
        setChanged(true);
    }

    public void setTileAccessibleAreaBlocks(int tileAccessibleAreaBlocks) {
        this.tileAccessibleAreaBlocks = tileAccessibleAreaBlocks;
        setChanged(true);
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

            setChanged(true);
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
    public ConfigType<MainConfig> getType() {
        return ConfigType.MAIN;
    }
}
