package org.minerift.ether.config.main;

import org.bukkit.NamespacedKey;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.source.FileSource;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.island.PurgeIslandsOption;
import org.minerift.ether.schematic.SchematicType;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class MainConfig extends Config<MainConfig> {

    public static final int CURRENT_VERSION = 0;

    public static final int CHUNK_SIZE = 16;
    public static final int MIN_TILE_CHUNKS = 3;
    public static final int MIN_TILE_SIZE = MIN_TILE_CHUNKS * CHUNK_SIZE; // 3 chunks * 16 blocks/chunk = 48 blocks

    private int version; // TODO: comment; DO NOT CHANGE!

    // purge settings
    private PurgeIslandsOption purgeIslandsOption;
    private int timeUntilNextIslandPurgeSecs; // in seconds
    private int purgedIslandsThreshold;

    private Map<String, Dimension> dimensions;

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
        this.version = CURRENT_VERSION;

        this.dimensions = new HashMap<>();
        dimensions.put("minecraft:overworld", new Dimension("overworld", new NamespacedKey("minecraft", "overworld"), 24, 12 * 16, 90));
        dimensions.put("minecraft:nether", new Dimension("nether", new NamespacedKey("minecraft", "nether"), 32, 24 * 16, 90));
        dimensions.put("minecraft:end", new Dimension("end", new NamespacedKey("minecraft", "end"), 32, 24 * 16, 90));

        this.inviteInvalidateAfter = TimeUnit.MINUTES.toMillis(2);

        this.dbType = Database.Type.SQL;
        this.sqlDialect = SQLDialect.H2;
        this.sqlUrl = "";
        this.sqlUsername = "root";
        this.sqlPassword = "";

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

    public Map<String, Dimension> getDimensions() {
        return dimensions;
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

    public Database.Type getDbType() {
        return dbType;
    }

    public void setDbType(Database.Type dbType) {
        this.dbType = dbType;
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

    public void setDimensions(Map<String, Dimension> dimensions) {
        this.dimensions = dimensions;

    }

    @Override
    protected void copyFrom(MainConfig o) {
        if(!o.equals(this)) {
            this.version = o.version;

            this.dimensions = o.dimensions;

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
    public boolean equals(Object o) {
        if (!(o instanceof MainConfig that)) return false;
        return version == that.version
                && timeUntilNextIslandPurgeSecs == that.timeUntilNextIslandPurgeSecs
                && purgedIslandsThreshold == that.purgedIslandsThreshold
                && inviteInvalidateAfter == that.inviteInvalidateAfter
                && purgeIslandsOption == that.purgeIslandsOption
                && Objects.equals(dimensions, that.dimensions)
                && dbType == that.dbType && sqlDialect == that.sqlDialect
                && Objects.equals(sqlUrl, that.sqlUrl) &&
                Objects.equals(sqlUsername, that.sqlUsername)
                && Objects.equals(sqlPassword, that.sqlPassword)
                && Objects.equals(defaultSchemType, that.defaultSchemType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version,
                dimensions,
                inviteInvalidateAfter,
                purgeIslandsOption, timeUntilNextIslandPurgeSecs, purgedIslandsThreshold,
                dbType,
                sqlDialect, sqlUrl, sqlUsername, sqlPassword,
                defaultSchemType);
    }

    @Override
    public ConfigType<MainConfig, FileSource> getType() {
        return ConfigType.MAIN;
    }
}
