package org.minerift.ether;

import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.work.WorkQueue;

import java.io.File;
import java.util.function.Supplier;
import java.util.logging.Logger;

// Provides static access to plugin components
// Must call load() before accessing any components
// TODO: add loadNoPlugin() method to load an instance without a plugin
public class Ether {

    /**
     * TODO: figure out what should be included in onLoad() vs. onEnable()
     */

    private static EtherPlugin plugin;
    private static ConfigRegistry configRegistry;
    private static Logger logger;
    private static File pluginDir;

    private static NMSAccess nmsAccess;
    private static WorkQueue workQueue;

    private static IslandManager islandManager;
    private static boolean isUsingWorldEdit;

    protected static void onLoad(EtherPlugin inst) {
        plugin = inst;
        pluginDir = plugin.getDataFolder();
        logger = plugin.getLogger();
    }

    protected static void onEnable() {
        // Load configs
        configRegistry = new ConfigRegistry();
        try {
            configRegistry.register(ConfigType.MAIN);
            configRegistry.register(ConfigType.SCHEM_LIST);
        } catch (ConfigFileReadException ex) {
            throw new RuntimeException(ex);
        }

        // For configs that don't exist, this will create a new file
        configRegistry.getAll().forEach(Config::save);


        isUsingWorldEdit = false;

        // Load work queue
        workQueue = new WorkQueue();
        workQueue.start();

        // Load NMS access
        nmsAccess = new NMSAccess();

        // Load managers
        islandManager = new IslandManager();
    }

    protected static void onDisable() {
        // Close work queue
        workQueue.close();
        workQueue = null;

        nmsAccess = null;

        plugin = null;
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
