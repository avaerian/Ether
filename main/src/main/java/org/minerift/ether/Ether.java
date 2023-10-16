package org.minerift.ether;

import com.google.common.base.Stopwatch;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.debug.*;
import org.minerift.ether.island.IslandInviteManager;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.user.UserManager;
import org.minerift.ether.work.WorkQueue;

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

// Provides static access to plugin components
// Must call load() before accessing any components
// TODO: add loadNoPlugin() method to load an instance without a plugin
public class Ether {

    private static boolean isLoaded;
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
        isLoaded = false;
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

        // For configs that don't exist, this will create a new file
        configRegistry.getAll().forEach(Config::save);

        stopwatch.stop();
        logger.info(String.format("Configs registered in %d ms", stopwatch.elapsed(TimeUnit.MILLISECONDS)));
        stopwatch.reset();

        MainConfig config = Ether.getConfig(ConfigType.MAIN);
        logger.info("tileSize: " + config.getTileSize());
        logger.info("tileHeight: " + config.getTileHeight());
        logger.info("tileAccessibleArea: " + config.getTileAccessibleArea());

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

        // Register debug commands
        plugin.getCommand("nmschunk").setExecutor(new NMSChunkDebugCommand());
        plugin.getCommand("nmsblock").setExecutor(new NMSSetBlocksDebugCommand());
        plugin.getCommand("blockscan").setExecutor(new NMSBlockScanDebugCommand());
        plugin.getCommand("pasteschem").setExecutor(new SchematicDebugCommand());
        plugin.getCommand("cfgreload").setExecutor(new ConfigReloadDebugCommand());

        //getLogger().info("Time elapsed: " + stopwatch.elapsed(TimeUnit.MILLISECONDS));

        isLoaded = true;
        logger.info("Ether plugin enabled!");
    }

    // For JavaPlugin
    protected static void onDisable() {
        if(isLoaded) {
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

        isLoaded = false;
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

    public static EtherPlugin getPlugin() {
        ensure(plugin != null, () -> new UnsupportedOperationException("plugin is not loaded!"));
        return plugin;
    }

    public static File getPluginDir() {
        ensure(pluginDir != null, () -> new UnsupportedOperationException("pluginDir is not loaded!"));
        return pluginDir;
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
        ensure(configRegistry != null, () -> new UnsupportedOperationException("configRegistry is not loaded!"));
        return configRegistry.get(type);
    }

    private static <E extends Exception> void ensure(boolean predicate, Supplier<E> ex) throws E {
        if(!predicate) {
            throw ex.get();
        }
    }
}
