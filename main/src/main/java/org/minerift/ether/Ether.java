package org.minerift.ether;

import org.bukkit.Bukkit;
import org.minerift.ether.config.Config;
import org.minerift.ether.config.ConfigRegistry;
import org.minerift.ether.config.exceptions.ConfigFileReadException;
import org.minerift.ether.config.types.ConfigType;
import org.minerift.ether.nms.NMSAccess;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.logging.Logger;

// Provides static access to plugin components
// Must call load() before accessing any components
// TODO: replace all EtherPlugin references with Ether
// TODO: add loadNoPlugin() method to load an instance without a plugin
public class Ether {

    private static EtherPlugin plugin;
    private static ConfigRegistry configRegistry;
    private static Logger logger;

    private static NMSAccess nmsAccess;

    protected static void load(EtherPlugin inst) {
        plugin = inst;
        //Bukkit.getServer().reload(); // TODO: view implementation to see how plugins reload
        logger = plugin.getLogger();

        // Load configs
        configRegistry = new ConfigRegistry();
        configRegistry.register(ConfigType.MAIN);
        configRegistry.register(ConfigType.SCHEM_LIST);

        // Load NMS access
        nmsAccess = new NMSAccess();
    }

    private Ether() {}

    public static ConfigRegistry getConfigRegistry() {
        ensure(configRegistry != null, () -> new UnsupportedOperationException("configRegistry was not loaded!"));
        return configRegistry;
    }

    public static Logger getLogger() {
        ensure(logger != null, () -> new UnsupportedOperationException("logger was not loaded!"));
        return logger;
    }

    public static NMSAccess getNMS() {
        ensure(nmsAccess != null, () -> new UnsupportedOperationException("nmsAccess was not loaded!"));
        return nmsAccess;
    }

    public static <T extends Config<T>> T getConfig(ConfigType<T> type) {
        ensure(configRegistry != null, () -> new UnsupportedOperationException("configRegistry was not loaded!"));
        return configRegistry.get(type);
    }

    private static <E extends Exception> void ensure(boolean predicate, Supplier<E> ex) throws E {
        if(!predicate) {
            throw ex.get();
        }
    }
}
