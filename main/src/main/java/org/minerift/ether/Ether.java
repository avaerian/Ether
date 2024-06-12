package org.minerift.ether;

import com.google.common.base.Stopwatch;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.island.IslandGridV2;
import org.minerift.ether.island.invites.InviteRegistry;
import org.minerift.ether.island.invites.IslandInviteManager;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.user.UserManager;
import org.minerift.ether.work.WorkQueue;

import java.io.File;
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
        nmsAccess = new NMSAccess();

        // Load managers
        islandManager = new IslandManager();
        inviteManager = new IslandInviteManager();
        userManager = new UserManager();

        stopwatch.stop();

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

            nmsAccess = null;
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

    public static NMSAccess getNMS() {
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
    public static class Debug {

        public static void setIslandManager(IslandManager manager) {
            islandManager = manager;
        }

        public static void setIslandGrid(IslandGridV2 grid) {
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
