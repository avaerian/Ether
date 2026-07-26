package org.minerift.ether.config.main;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.source.FileSource;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.dimension.Dimensions;
import org.minerift.ether.island.PurgeIslandsOption;
import org.minerift.ether.schematic.SchematicType;

import java.util.concurrent.TimeUnit;

@EqualsAndHashCode(callSuper = false)
public class MainConfig extends Config<MainConfig> {

    public static final int CURRENT_VERSION = 0;

    public static final int CHUNK_SIZE = 16;
    public static final int MIN_TILE_CHUNKS = 3;
    public static final int MIN_TILE_SIZE = MIN_TILE_CHUNKS * CHUNK_SIZE; // 3 chunks * 16 blocks/chunk = 48 blocks

    // TODO: ensure case is handled where less deleted tiles exist than min purged islands
    public static final int DEFAULT_MIN_PURGED_ISLANDS = 0;
    public static final int DEFAULT_MAX_PURGED_ISLANDS = Integer.MAX_VALUE;

    private int version; // TODO: comment; DO NOT CHANGE!

    // purge settings
    private PurgeIslandsOption purgeIslandsOption;
    private int timeUntilNextIslandPurgeSecs; // in seconds
    private int purgedIslandsThreshold;
    @Getter @Setter private int maxPurgedIslands; // per clear
    @Getter @Setter private int minPurgedIslands; // per clear

    private Dimensions dims;

    private long inviteInvalidateAfter; // ms time unit

    private Database.Type dbType;
    private SQLDialect sqlDialect;
    private String sqlUrl; // optional, depending on dialect
    private String sqlUsername;
    private String sqlPassword;

    private SchematicType<?> defaultSchemType;

    private boolean tpAfterVoidJump; // teleport user back to the island after jumping into void

    // Default values for config
    public MainConfig(ConfigRegistry reg, FileSource src) {
        super(reg, src);
        this.version = CURRENT_VERSION;

        this.dims = Dimensions.minecraft();

        // island invites
        this.inviteInvalidateAfter = TimeUnit.MINUTES.toMillis(2);

        // persistence
        this.dbType = Database.Type.SQL;
        this.sqlDialect = SQLDialect.H2;
        this.sqlUrl = "";
        this.sqlUsername = "root";
        this.sqlPassword = "";

        // island purge management
        this.purgeIslandsOption = PurgeIslandsOption.QUEUED;
        this.timeUntilNextIslandPurgeSecs = 60 * 5;
        this.purgedIslandsThreshold = 5;

        this.defaultSchemType = SchematicType.SPONGE;
    }

    // Getters
    public int getVersion() {
        return version;
    }

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

    public PurgeIslandsOption getPurgeIslandsOption() {
        return purgeIslandsOption;
    }

    public Dimensions getDimensions() {
        return dims;
    }

    public long getInviteInvalidateAfter() {
        return inviteInvalidateAfter;
    }

    public SchematicType<?> getDefaultSchemType() {
        return defaultSchemType;
    }

    public PurgeIslandsOption purgeIslandsOption() {
        return purgeIslandsOption;
    }

    public int getPurgedIslandsThreshold() {
        return purgedIslandsThreshold;
    }

    public int getTimeUntilNextIslandPurgeSecs() {
        return timeUntilNextIslandPurgeSecs;
    }

    // Setters
    public void setVersion(int version) {
        this.version = version;
    }

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

    public void setDefaultSchemType(SchematicType<?> type) {
        this.defaultSchemType = type;
    }

    public void setPurgeIslandsOption(PurgeIslandsOption option) {
        this.purgeIslandsOption = option;
    }

    public void setTimeUntilNextIslandPurgeSecs(int timeUntilNextIslandPurgeSecs) {
        this.timeUntilNextIslandPurgeSecs = timeUntilNextIslandPurgeSecs;
    }

    public void setPurgedIslandsThreshold(int threshold) {
        this.purgedIslandsThreshold = threshold;
    }

    public void setDimensions(Dimensions dims) {
        this.dims = dims;
    }

    @Override
    protected void copyFrom(MainConfig o) {
        if(!o.equals(this)) {
            this.version = o.version;

            this.dims = o.dims;

            this.inviteInvalidateAfter = o.inviteInvalidateAfter;

            this.dbType = o.dbType;
            this.sqlDialect = o.sqlDialect;
            this.sqlUrl = o.sqlUrl;
            this.sqlUsername = o.sqlUsername;
            this.sqlPassword = o.sqlPassword;

            this.defaultSchemType = o.defaultSchemType;

            this.purgeIslandsOption = PurgeIslandsOption.QUEUED;
            this.timeUntilNextIslandPurgeSecs = 60 * 5;
            this.purgedIslandsThreshold = 5;
        }
    }



    @Override
    public ConfigType<MainConfig, FileSource> getType() {
        return ConfigType.MAIN;
    }
}
