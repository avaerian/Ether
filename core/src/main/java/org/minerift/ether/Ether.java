package org.minerift.ether;

import org.minerift.ether.config.*;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.config.source.DirectorySource;
import org.minerift.ether.config.source.FileSource;
import org.minerift.ether.database.*;
import org.minerift.ether.database.models.IslandModel;
import org.minerift.ether.database.models.UserModel;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandManager;
import org.minerift.ether.island.invites.IslandInviteManager;
import org.minerift.ether.nms.NMS;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.user.UserManager;
import org.minerift.ether.work.WorkQueue;
import org.minerift.ether.util.log.StageTimekeeper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.minerift.ether.Ether.Stage.*;

// Provides static access to plugin components
// TODO: support unloaded and loaded Ether instance for IDE & server usage
public class Ether /*implements AutoCloseable*/ {

    public enum Stage {
        STAGE_CFGS,
        STAGE_DB,
        STAGE_ISLANDS,
        STAGE_INVITES,
        STAGE_USERS,
        STAGE_WORK_QUEUE,
        STAGE_NMS
    }

    // not worried about synchronization; single-threaded impl
    public static class InitResult {
        public final Ether ether;
        public final StageTimekeeper<Stage> tracker;

        protected InitResult(Ether ether, StageTimekeeper<Stage> tracker) {
            this.ether = ether;
            this.tracker = tracker;
        }

        public long getLoadTime() {
            return tracker.getLoadTime(NANOSECONDS);
        }

        public long getLoadTime(TimeUnit unit) {
            return tracker.getLoadTime(unit);
        }

        // default time unit is nanoseconds
        public long getLoadTime(Stage... stages) {
            return tracker.getLoadTime(NANOSECONDS, stages);
        }

        public long getLoadTime(TimeUnit unit, Stage... stages) {
            return tracker.getLoadTime(unit, stages);
        }
    }
    
    public static Ether.InitResult from(File dataDir, Logger logger) throws EtherLoadException {
        System.setProperty("org.jooq.no-logo", "true");
        System.setProperty("org.jooq.no-tips", "true");

        final StageTimekeeper<Stage> times = new StageTimekeeper<>(Stage.class);

        // load configs
        times.start();
        ConfigRegistry cfgs = new ConfigRegistry(dataDir, logger);
        try {
            cfgs.register(ConfigType.MAIN, FileSource.of(new File(dataDir, "config.yml")));
            cfgs.register(ConfigType.ISLAND_SPECS_LIST, DirectorySource.of(new File(dataDir, "island_specs")));
        } catch (IOException e) {
            // If failed, log error and abort plugin loading
            // Failing will be delegated to EtherPlugin or other bootstrapper
            throw new EtherLoadException("Failed to register configs", e);
        }

        // for configs that don't exist, this will save to their respective sources
        for(ConfigRegistry.Entry entry : cfgs) {
            try {
                entry.config.save();
            } catch (ConfigWriteException e) {
                logger.log(Level.SEVERE, "Failed to save " + entry.config.getTypeName(), e);
            }
        }
        long cfgMs = times.trackAndReset(STAGE_CFGS, MILLISECONDS);
        logger.info(String.format("Configs registered in %d ms", cfgMs));

        MainConfig config = cfgs.get(ConfigType.MAIN);
        logger.info("tileSize: " + config.getTileLengthChunks());
        logger.info("tileHeight: " + config.getTileHeight());
        logger.info("tileAccessibleArea: " + config.getTileAccessibleAreaBlocks());

        // Load work queue
        times.start();
        WorkQueue workQueue = new WorkQueue();
        workQueue.start();
        long wqMs = times.trackAndReset(STAGE_WORK_QUEUE, MILLISECONDS);

        // Load NMS access
        times.start();
        NMSAccess nms = NMS.createAccess();
        long nmsMs = times.trackAndReset(STAGE_NMS, MILLISECONDS);

        // Load managers
        times.start();
        IslandManager islands = new IslandManager(); // TODO: This needs to be delayed until islands are loaded from db
        times.trackAndReset(STAGE_ISLANDS);

        times.start();
        IslandInviteManager invites = new IslandInviteManager(); // TODO: This needs to be delayed until invites are loaded from db
        
        UserManager users = new UserManager();

        // Connect to database and load data
        var login = DatabaseConnectionSettings.builder()
                .setDbName("ether")
                .setUrl(dataDir.getAbsolutePath())
                .setDialect(config.getSqlDialect())
                .setUsername(config.getSqlUsername())
                .setPassword(config.getSqlPassword())
                .build();

        Database db = new SQLDatabase(login, IslandModel::new, UserModel::new);
        try {
            DatabaseException result = db.accessSync((access) -> {
                IslandModel islandModel = access.getModel(IslandModel.class);

                Result<EtherUser> usersResult = access.selectAll(UserModel.class);
                Result<Island> islandsResult = access.selectAll(IslandModel.class);

                //UUID[] uuids = islandsResult.getRecord(0).get(islandModel.MEMBERS);

                //List<String> t = Collections.emptyList(); // NOTE: experiment
                //Class<List<String>> c = (Class<List<String>>) Collections.emptyList().getClass(); // NOTE: experiment
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new EtherLoadException("unexpected", e);
        }

        // ** code for plugin command registration has been moved to EtherPlugin **
        //getLogger().info("Time elapsed: " + stopwatch.elapsed(

        Ether ether = new Ether(cfgs, logger, dataDir,
                db, nms, workQueue, 
                islands, invites, users);
        return new InitResult(ether, times);
    }

    // Considering different modules (i.e. Paper, Fabric), this inst
    // can be updated by having a bootstrapper class in the package
    // org.minerift.ether, which will provide access to this for init.
    /**
     * This field needs to be set to a proper Ether instance
     * created by the Paper, Fabric, or other game-supported
     * modules before using in-game, otherwise a debug instance
     * will be created if the run flag is set, or an uninit instance
     * which will throw for all component getters.
     */
    protected static Ether INST = null;
    protected static boolean IS_DEBUG = false;

    // For IDE debugging, set -Dether.runInIde=true flag.
    // When the plugin loads, a new Ether instance will
    // be generated and set from the plugin.
    public static Ether inst() {
        if(INST != null) {
            return INST;
        }
        String debug = System.getProperty("ether.runInIde");
        if(debug != null && debug.equalsIgnoreCase("true")) { // TODO: review this
            INST = new Debug();
            IS_DEBUG = true;
        } else {
            INST = new Uninit();
        }
        return INST;
    }

    public static Ether.Debug debug() {
        if(INST == null) {
            INST = new Debug();
            IS_DEBUG = true;
            return (Debug) INST;
        }
        if(IS_DEBUG) {
            return (Debug) INST;
        } else {
            throw new UnsupportedOperationException("Ether instance is not a Debug instance");
        }
    }

    protected Database db;
    protected ConfigRegistry cfgs;
    protected Logger log;
    protected File dataDir;

    protected NMSAccess nms;
    protected WorkQueue workQueue;

    protected IslandManager islands;
    protected IslandInviteManager invites;
    protected UserManager users;

    public Ether(ConfigRegistry cfgs, Logger log, File dataDir,
                Database db, NMSAccess nms, WorkQueue workQueue,
                IslandManager islands, IslandInviteManager invites,
                UserManager users) {
        this.cfgs = cfgs;
        this.log = log;
        this.dataDir = dataDir;
        this.db = db;
        this.nms = nms;
        this.workQueue = workQueue;
        this.islands = islands;
        this.invites = invites;
        this.users = users;
    }

    public File getDataDir() {
        return dataDir;
    }

    // TODO: rename
    public File getPluginFile(String fileName) {
        return new File(getDataDir(), fileName);
    }

    public Database getDatabase() {
        return db;
    }

    public ConfigRegistry getConfigRegistry() {
        return cfgs;
    }

    public <T extends Config<T>> T getConfig(ConfigType<T, ?> type) {
        return cfgs.get(type);
    }

    public WorkQueue getWorkQueue() {
        return workQueue;
    }

    public NMSAccess getNms() {
        return nms;
    }

    public IslandManager getIslandManager() {
        return islands;
    }

    public IslandInviteManager getInviteManager() {
        return invites;
    }

    public UserManager getUserManager() {
        return users;
    }
    
    public Logger getLogger() {
        return log;
    }

    protected void close() {
        for(Config config : cfgs.getAll()) {
            try {
                config.save();
            } catch (ConfigWriteException e) {
                throw new RuntimeException(e);
            }
        }
        cfgs = null;

        workQueue.close();
        workQueue = null;

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

        log = null;
        dataDir = null;
    }

    public static class Debug extends Ether {
        public Debug(ConfigRegistry cfgs, Logger log, File dataDir,
                Database db, NMSAccess nms, WorkQueue workQueue,
                IslandManager islands, IslandInviteManager invites,
                UserManager users) {
            super(cfgs, log, dataDir,
                    db, nms, workQueue,
                    islands, invites, users);
        }

        public Debug() {
            super(null, null, null,
                    null, null, null,
                    null, null, null);
        }

        public void setIslandManager(IslandManager islands) {
            this.islands = islands;
        }

        public void setIslandInviteManager(IslandInviteManager invites) {
            this.invites = invites;
        }

        public void setUserManager(UserManager users) {
            this.users = users;
        }

        public void setLogger(Logger log) {
            this.log = log;
        }
    }

    public static class Uninit extends Ether {
        public static final String EX_MSG = "Ether seems to be uninitialized";

        protected Uninit() {
            super(null, null, null,
                    null, null, null,
                    null, null, null);
        }

        @Override
        public File getDataDir() {
            throw new RuntimeException(new EtherLoadException(EX_MSG));
        }
    
        @Override
        public Database getDatabase() {
            throw new RuntimeException(new EtherLoadException(EX_MSG));
        }
    
        @Override
        public ConfigRegistry getConfigRegistry() {
            throw new RuntimeException(new EtherLoadException(EX_MSG));
        }
    
        @Override
        public WorkQueue getWorkQueue() {
            throw new RuntimeException(new EtherLoadException(EX_MSG));
        }
    
        @Override
        public NMSAccess getNms() {
            throw new RuntimeException(new EtherLoadException(EX_MSG));
        }
    
        @Override
        public IslandManager getIslandManager() {
            throw new RuntimeException(new EtherLoadException(EX_MSG));
        }
    
        @Override
        public IslandInviteManager getInviteManager() {
            throw new RuntimeException(new EtherLoadException(EX_MSG));
        }
    
        @Override
        public UserManager getUserManager() {
            throw new RuntimeException(new EtherLoadException(EX_MSG));
        }
        
        @Override
        public Logger getLogger() {
            throw new RuntimeException(new EtherLoadException(EX_MSG));
        }

        @Override
        public void close() {
            // FIXME: no-op?
        }
    }

    // TODO: needs review
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

    // TODO: review
    /*public static ConfigRegistry getConfigRegistry() {
        return init().getConfigRegistry();
    }

    public static <T extends Config<T>> T getConfig(ConfigType<T> type) {
        return init().getConfigRegistry().get(type);
    }

    public static Logger getLogger() {
        return init().log;
    }

    public static File getPluginDir() {
        return init().getPluginDir();
    }

    public static File getPluginFile(String path) {
        return new File(getPluginDir(), path);
    }

    public static File getPluginFile(Directory dir, String path) {
        return getPluginFile(dir.getDirName() + File.separator + path);
    }

    public static Database getDatabase() {
        return init().getDatabase();
    }

    public static NMSAccess getNms() {
        return init().getNms();
    }

    public static WorkQueue getWorkQueue() {
        return init().getWorkQueue();
    }

    public static IslandManager getIslandManager() {
        return init().getIslandManager();
    }

    public static IslandInviteManager getInviteManager() {
        return init().getInviteManager();
    }

    public static UserManager getUserManager() {
        return init().getUserManager();
    }*/
}
