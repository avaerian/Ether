package org.minerift.ether;

import com.google.common.base.Stopwatch;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.DatabaseException;
import org.minerift.ether.database.Result;
import org.minerift.ether.database.models.NuIslandModel;
import org.minerift.ether.database.models.NuUserModel;
import org.minerift.ether.database.nusql.NuSQLDatabase;
import org.minerift.ether.island.DefaultIslandGrid;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.island.invites.IslandInviteManager;
import org.minerift.ether.nms.DeprecatedNMSAccess;
import org.minerift.ether.nms.NMS;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.user.UserManager;
import org.minerift.ether.work.WorkQueue;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.minerift.ether.util.Utils.ensure;

// Provides static access to plugin components
// Must call load() before accessing any components
// TODO: add loadNoPlugin() method to load an instance without a plugin
public class Ether {

    private static boolean isEnabled;
    private static EtherPlugin plugin;
    private static ConfigRegistry configRegistry;
    private static Logger logger;
    private static File pluginDir;

    private static Database db;
    @Deprecated private static DeprecatedNMSAccess depNmsAccess;
    private static NMSAccess nmsAccess;
    private static WorkQueue workQueue;

    private static IslandManager islandManager;
    private static IslandInviteManager inviteManager;
    private static UserManager userManager;
    private static boolean isUsingWorldEdit;

    // For JavaPlugin
    protected static void onLoad(EtherPlugin inst) {
        isEnabled = false;
        plugin = inst;
        pluginDir = plugin.getDataFolder();
        logger = plugin.getLogger();
        // TODO: debug logger?
    }

    // For JavaPlugin
    protected static void onEnable() {

        final Stopwatch stopwatch = Stopwatch.createStarted();

        // Load configs
        configRegistry = new ConfigRegistry();
        try {
            configRegistry.register(ConfigType.MAIN);
            //configRegistry.register(ConfigType.SCHEM_LIST);
        } catch (ConfigFileReadException ex) {
            // If failed, log error and abort plugin loading
            logger.log(Level.SEVERE, "Failed to register configs when enabling Ether: ", ex);
            plugin.disable();
            return;
        }

        // For config files that don't exist, this will create a new file
        configRegistry.getAll().forEach(Config::save);

        stopwatch.stop();
        logger.info(String.format("Configs registered in %d ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        stopwatch.reset();

        MainConfig config = Ether.getConfig(ConfigType.MAIN);
        logger.info("tileSize: " + config.getTileLengthChunks());
        logger.info("tileHeight: " + config.getTileHeight());
        logger.info("tileAccessibleArea: " + config.getTileAccessibleAreaBlocks());

        stopwatch.start();

        isUsingWorldEdit = false;

        // Load work queue
        workQueue = new WorkQueue();
        workQueue.start();

        // Load NMS access
        depNmsAccess = new DeprecatedNMSAccess(); // TODO: remove
        nmsAccess = NMS.createAccess();


        // Load managers
        //islandManager = new IslandManager(); // This needs to be delayed until islands are loaded
        inviteManager = new IslandInviteManager(); // This needs to be delayed until invites are loaded
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

        db = new NuSQLDatabase(login, NuIslandModel::new, NuUserModel::new);
        try {
            DatabaseException result = db.accessSync((access) -> {
                var islandModel = access.getModel(NuIslandModel.class);

                Result<EtherUser> usersResult = access.selectAll(NuUserModel.class);
                Result<Island> islandsResult = access.selectAll(NuIslandModel.class);

                //islandsResult.streamBuilders().
            }).get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException("Unexpected", ex);
        }

        // ** code for plugin command registration has been moved to EtherPlugin **
        //getLogger().info("Time elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));

        isEnabled = true;
        logger.info("Ether plugin enabled!");
    }

    // For JavaPlugin
    protected static void onDisable() {
        if(isEnabled) {
            configRegistry.getAll().forEach(Config::saveIfChanged);
            configRegistry = null;

            // Close work queue
            workQueue.close();
            workQueue = null;

            depNmsAccess = null;
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

    public static DeprecatedNMSAccess getDeprecatedNMS() {
        ensure(depNmsAccess != null, () -> new UnsupportedOperationException("depNmsAccess is not loaded!"));
        return depNmsAccess;
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
