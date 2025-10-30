package org.minerift.ether;

import com.google.common.base.Stopwatch;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.ConfigFileReadException;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.DatabaseException;
import org.minerift.ether.database.Result;
import org.minerift.ether.database.models.IslandModel;
import org.minerift.ether.database.models.UserModel;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.island.DefaultIslandGrid;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.island.invites.IslandInviteManager;
import org.minerift.ether.nms.NMS;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.schematic.SchematicType;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.user.UserManager;
import org.minerift.ether.work.WorkQueue;
import org.minerift.ether.debug.NeedsTesting;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.minerift.ether.util.Utils.ensure;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

// Provides static access to plugin components
// TODO: support unloaded and loaded Ether instance for IDE & server usage
public class Ether implements AutoCloseable {
    
    // unordered init stages
    public static final int STAGE_CFGS;
    public static final int STAGE_DB;
    public static final int STAGE_ISLANDS;
    public static final int STAGE_INVITES;
    public static final int STAGE_USERS;
    public static final int STAGE_WORK_QUEUE;
    public static final int STAGE_NMS;
    
    public static final int STAGES_COUNT;

    static {
        int i = 0;

        STAGE_CFGS       = 1 << i++; // 1, 0
        STAGE_DB         = 1 << i++; // 2, 1
        STAGE_ISLANDS    = 1 << i++; // 4, 2
        STAGE_INVITES    = 1 << i++; // 8, 3
        STAGE_USERS      = 1 << i++; // 16, 4
        STAGE_WORK_QUEUE = 1 << i++; // 32, 5
        STAGE_NMS        = 1 << i++; // 64, 6
        //PLUGIN     = 1 << i++; // 128, 7; TODO: review

        STAGES_COUNT = i; // 8
    }

    // not worried about synchronization; single-threaded impl
    public static class InitResult {
        public final Ether ether;
        public final long[] epochsNs;

        protected InitResult(Ether ether, long[] epochsNs) {
            this.ether = ether;
            this.epochsNs = epochsNs;
        }

        @NeedsTesting
        @Deprecated // should already have everything resolved when creating
        public long register(int stage, long epochNs) {
            if(stage == 0) {
                throw new IllegalArgumentException("No stages selected to register epoch");
            }
            if( (stage & (stage - 1)) != 0 ) { // ensure only one stage is selected; check if pow of 2
                throw new IllegalArgumentException("Unable to register epoch for multiple stages");
            }
            
            int i = Integer.numberOfTrailingZeros(stage);
            epochsNs[i] = epochNs;
            return epochNs;
        }

        // not a fan of this, but don't want to write this out every goddamn time
        protected static int index(int stage) {
            return Integer.numberOfTrailingZeros(stage);
        }

        public long getLoadTime() {
            return getLoadTime(STAGES_COUNT - 1, NANOSECONDS);
        }

        public long getLoadTime(TimeUnit unit) {
            return getLoadTime(STAGES_COUNT - 1, unit);
        }

        // default time unit is nanoseconds
        public long getLoadTime(int stages) {
            return getLoadTime(stages, NANOSECONDS);
        }

        public long getLoadTime(int stages, TimeUnit unit) {
            if(stages == 0) {
                throw new IllegalArgumentException("No stages selected to query load time");
            }
            int n = stages;
            int i;
            long sum = 0;
            while((i = Integer.numberOfTrailingZeros(n)) != 32){
                sum += epochsNs[i];
                n &= ~(1 << i);
            }
            return unit.convert(sum, NANOSECONDS);
        }

        
    }
    
    public static Ether from(File pluginDir) throws EtherLoadException {

        final long[] epochsNs = new long[STAGES_COUNT];
        final Stopwatch stopwatch = Stopwatch.createStarted();

        // load configs
        ConfigRegistry cfgs = new ConfigRegistry();
        try {
            cfgs.register(ConfigType.MAIN);
            cfgs.register(ConfigType.ISLAND_SPECS_LIST);
        } catch (ConfigFileReadException e) {
            // If failed, log error and abort plugin loading
            // Failing will be delegated to EtherPlugin or other bootstrapper
            throw new EtherLoadException("Failed to register configs, e);
        }

        // for config files that don't exist, this will create a new file
        cfgs.getAll().forEach(Config::save);
        {
            stopwatch.stop();
            epochsNs[InitResult.index(STAGE_CFGS)] = stopwatch.elapsed();
            logger.info(String.format("Configs registered in %d ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
            stopwatch.reset();
        }

        MainConfig config = Ether.getConfig(ConfigType.MAIN);
        logger.info("tileSize: " + config.getTileLengthChunks());
        logger.info("tileHeight: " + config.getTileHeight());
        logger.info("tileAccessibleArea: " + config.getTileAccessibleAreaBlocks());

        // Load work queue
        stopwatch.start();
        workQueue = new WorkQueue();
        workQueue.start();
        stopwatch.stop();
        epochsNs[InitResult.index(STAGE_WORK_QUEUE)] = stopwatch.elapsed();
        stopwatch.reset();

        // Load NMS access
        stopwatch.start();
        nmsAccess = NMS.createAccess();
        stopwatch.stop();
        epochsNs[InitResult.index(STAGE_NMS)] = stopwatch.elapsed();
        stopwatch.reset();

        // Load managers
        islandManager = new IslandManager(); // TODO: This needs to be delayed until islands are loaded
        inviteManager = new IslandInviteManager(); // TODO: This needs to be delayed until invites are loaded
        userManager = new UserManager();

        stopwatch.stop();

        // Connect to database and load data
        var login = DatabaseConnectionSettings.builder()
                .setDbName("ether")
                .setUrl(Ether.getPluginDir().getAbsolutePath())
                .setDialect(config.getSqlDialect())
                .setUsername(config.getSqlUsername())
                .setPassword(config.getSqlPassword())
                .build();

        db = new SQLDatabase(login, IslandModel::new, UserModel::new);
        try {
            DatabaseException result = db.accessSync((access) -> {
                var islandModel = access.getModel(IslandModel.class);

                Result<EtherUser> usersResult = access.selectAll(UserModel.class);
                Result<Island> islandsResult = access.selectAll(IslandModel.class);

                //islandsResult.streamBuilders().
            }).get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException("Unexpected", ex);
        }

        // ** code for plugin command registration has been moved to EtherPlugin **
        //getLogger().info("Time elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));

        enabled = true;
        logger.info("Ether plugin enabled");
    }

    protected boolean enabled;
    /*@Deprecated protected EtherPlugin plugin;*/
    protected ConfigRegistry cfgs;
    protected Logger log;
    protected File pluginDir;

    protected Database db;
    protected NMSAccess nms;
    protected WorkQueue workQueue;

    protected IslandManager islands;
    protected IslandInviteManager invites;
    protected UserManager users;

    public static Ether INST = null;

    public Ether(/*EtherPlugin plugin,*/
                ConfigRegistry cfgs, Logger log, File pluginDir,
                Database db, NMSAccess nms, WorkQueue workQueue,
                IslandManager islands, IslandInviteManager invites,
                UserManager users/*, boolean isUsingWorldEdit*/) {
        this.plugin = plugin;
        this.cfgs = cfgs;
        this.log = log;
        this.pluginDir = pluginDir;
        this.db = db;
        this.nms = nms;
        this.workQueue = workQueue;
        this.islands = islands;
        this.invites = invites;
        this.users = users;
        this.enabled = true;
    }

    @Deprecated /* TODO: review */
    public boolean isEnabled() {
        return enabled;
    }

    @Deprecated /* TODO: review */
    public EtherPlugin plugin() {
        return plugin;
    }

    public File getPluginDir() {
        return pluginDir;
    }

    public Database getDatabase() {
        return db;
    }

    public ConfigRegistry getConfigRegistry() {
        return cfgs;
    }

    public WorkQueue getWorkQueue() {
        return workQueue;
    }

    public NMSAccess getNms()
        return nms;
    }

    public IslandManager getIslandManager() {
        return islands;
    }

    public IslandInvitesManager getIslandInvitesManager() {
        return invites;
    }

    public UserManager getUserManager() {
        return users;
    }
    
    public Logger getLogger() {
        return log;
    ]

    @Deprecated
    protected static void onLoad(EtherPlugin inst) {
        isEnabled = false;
        plugin = inst;
        pluginDir = plugin.getDataFolder();
        logger = plugin.getLogger();
        // TODO: debug logger?
    }

    @Override
    public void close() {
        if(isEnabled) {
            configRegistry.getAll().forEach(Config::saveIfChanged);
            configRegistry = null;

            // Close work queue
            workQueue.close();
            workQueue = null;

        }

        if(db != null) {
            try {
                db.close();
            } catch (DatabaseException ex) {
                // handle here, if anything's needed
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            db = null;
        }

        logger = null;
        pluginDir = null;
        plugin = null;

        isEnabled = false;
    }

    private Ether() {}

    public static ConfigRegistry getConfigRegistry() {
        ensure(configRegistry != null, () -> new UnsupportedOperationException("configRegistry is not loaded!"));
        return configRegistry;
    }

    public static Logger getLogger() {
        ensure(logger != null, () -> new UnsupportedOperationException("logger is not loaded!"));
        return logger;
    }

    public static EtherPlugin plugin() {
        ensure(plugin != null, () -> new UnsupportedOperationException("plugin is not loaded!"));
        return plugin;
    }

    public static File getPluginDir() {
        ensure(pluginDir != null, () -> new UnsupportedOperationException("pluginDir is not loaded!"));
        return pluginDir;
    }

    public static File getPluginFile(String path) {
        return new File(getPluginDir(), path);
    }

    public static Database getDatabase() {
        ensure(db != null, () -> new UnsupportedOperationException("db is not loaded!"));
        return db;
    }

    public enum Directory {
        SCHEMATICS("schems"),

        ;

        private String dirName;
        Directory(String dirName) {
            this.dirName = dirName;
        }

        public String getDirName() {
            return dirName;
        }
    }

    public static File getPluginFile(Directory dir, String path) {
        return getPluginFile(dir.getDirName() + File.separator + path);
    }

    public static boolean isUsingWorldEdit() {
        return isUsingWorldEdit;
    }

    public static NMSAccess getNms() {
        ensure(nmsAccess != null, () -> new UnsupportedOperationException("nmsAccess is not loaded!"));
        return nmsAccess;
    }

    public static WorkQueue getWorkQueue() {
        ensure(workQueue != null, () -> new UnsupportedOperationException("workQueue is not loaded!"));
        return workQueue;
    }

    public static IslandManager getIslandManager() {
        ensure(islandManager != null, () -> new UnsupportedOperationException("islandManager is not loaded!"));
        return islandManager;
    }

    public static IslandInviteManager getInviteManager() {
        ensure(inviteManager != null, () -> new UnsupportedOperationException("inviteManager is not loaded!"));
        return inviteManager;
    }

    public static UserManager getUserManager() {
        ensure(userManager != null, () -> new UnsupportedOperationException("userManager is not loaded!"));
        return userManager;
    }

    public static <T extends Config<T>> T getConfig(ConfigType<T> type) {
        return getConfigRegistry().get(type);
    }

    /**
     * Only to be used when debugging
     */
    @org.minerift.ether.debug.Debug
    public static class Debug {

        public static void setIslandManager(IslandManager manager) {
            islandManager = manager;
        }

        public static void setIslandGrid(DefaultIslandGrid grid) {
            setIslandManager(new IslandManager(grid));
        }

        public static void setIslandInviteManager(IslandInviteManager manager) {
            inviteManager = manager;
        }

        public static void setUserManager(UserManager manager) {
            userManager = manager;
        }

        public static void setLogger(Logger logger) {
            Ether.logger = logger;
        }

        private Debug() {}
    }
}
