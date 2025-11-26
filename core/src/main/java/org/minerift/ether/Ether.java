package org.minerift.ether;

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
import org.minerift.ether.util.log.StagedTimekeeper;
import org.minerift.ether.util.UnreachableException;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.minerift.ether.util.Utils.ensure;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

// Provides static access to plugin components
// TODO: support unloaded and loaded Ether instance for IDE & server usage
public class Ether implements AutoCloseable {
    
    public static ConfigRegistry getConfigRegistry() {
        return init().getConfigRegistry();
    }

    public static <T extends Config<T>> T getConfig(ConfigType<T> type) {
        return init().getConfigRegistry().get(type);
    }

    public static Logger getLogger() {
        return init().getLogger();
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
    }
    
    // unordered init stages
    public static final int STAGE_CFGS;
    public static final int STAGE_DB;
    public static final int STAGE_ISLANDS;
    public static final int STAGE_INVITES;
    public static final int STAGE_USERS;
    public static final int STAGE_WORK_QUEUE;
    public static final int STAGE_NMS;
    
    public static final int ALL_STAGES;
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

        STAGES_MASK = (1 << i) - 1;
        STAGES_COUNT = i; // 7
    }

    // not worried about synchronization; single-threaded impl
    public static class InitResult {
        public final Ether ether;
        public final StagedTimekeeper tracker;

        protected InitResult(Ether ether, StagedTimekeeper tracker) {
            this.ether = ether;
            this.tracker = tracker;
        }

        public long getLoadTime() {
            return tracker.getLoadTime(STAGES_MASK, NANOSECONDS);
        }

        public long getLoadTime(TimeUnit unit) {
            return tracker.getLoadTime(STAGES_MASK, unit);
        }

        // default time unit is nanoseconds
        public long getLoadTime(int stages) {
            return tracker.getLoadTime(stages, NANOSECONDS);
        }

        public long getLoadTime(int stages, TimeUnit unit) {
            return tracker.getLoadTime(stages, unit);
        }
    }
    
    public static Ether.InitResult from(File pluginDir, Logger logger) throws EtherLoadException {

        final StagedTimekeeper.Builder times = StagedTimekeeper.builder(stopwatch, STAGES_COUNT - 1);

        // load configs
        times.start();
        ConfigRegistry cfgs = new ConfigRegistry(pluginDir);
        try {
            cfgs.register(ConfigType.MAIN);
            cfgs.register(ConfigType.ISLAND_SPECS_LIST);
        } catch (ConfigFileReadException e) {
            // If failed, log error and abort plugin loading
            // Failing will be delegated to EtherPlugin or other bootstrapper
            throw new EtherLoadException("Failed to register configs", e);
        }

        // for config files that don't exist, this will create a new file
        cfgs.getAll().forEach(Config::save);
        long cfgMs = times.trackAndReset(STAGE_CFGS, MILLISECONDS);
        logger.info(String.format("Configs registered in %d ms", cfgMs));

        MainConfig config = Ether.getConfig(ConfigType.MAIN);
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
        IslandManager islands = new IslandManager(); // TODO: This needs to be delayed until islands are loaded
        times.trackAndReset(STAGE_ISLANDS);

        times.start();
        InviteManager invites = new IslandInviteManager(); // TODO: This needs to be delayed until invites are loaded
        
        UserManager users = new UserManager();

        // Connect to database and load data
        var login = DatabaseConnectionSettings.builder()
                .setDbName("ether")
                .setUrl(Ether.getPluginDir().getAbsolutePath())
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

                //islandsResult.streamBuilders().
            }).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new EtherLoadException("unexpected", e);
        }

        // ** code for plugin command registration has been moved to EtherPlugin **
        //getLogger().info("Time elapsed: " + stopwatch.elapsed(

        Ether ether = new Ether(cfgs, logger, pluginDir, 
                db, nms, workQueue, 
                islands, invites, users);
        return new InitResult(ether, times.build());
    }

    // for IDE debugging, set -Dether.runInIde=true
    // when the plugin loads, a new Ether instance
    // will be generated and set from the plugin
    // TODO
    protected static Ether init() {
        String debug = System.getProperty("ether.runInIde");
        if(debug != null && debug.equalsIgnoreCase("true")) {
            /*TODO: INST = Ether.builder()*/
        } else {
            INST = new Uninit();
        }
        return INST;
    }

    // exposed; access at own risk
    @Deprecated
    public static Ether INST = null;

    protected Database db;
    protected ConfigRegistry cfgs;
    protected Logger log;
    protected File pluginDir;

    protected NMSAccess nms;
    protected WorkQueue workQueue;

    protected IslandManager islands;
    protected IslandInviteManager invites;
    protected UserManager users;

    public Ether(ConfigRegistry cfgs, Logger log, File pluginDir,
                Database db, NMSAccess nms, WorkQueue workQueue,
                IslandManager islands, IslandInviteManager invites,
                UserManager users) {
        this.cfgs = cfgs;
        this.log = log;
        this.pluginDir = pluginDir;
        this.db = db;
        this.nms = nms;
        this.workQueue = workQueue;
        this.islands = islands;
        this.invites = invites;
        this.users = users;
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

    public NMSAccess getNms() {
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
    }

    @Override
    public void close() {
        cfgs.getAll().forEach(Config::saveIfChanged);
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

        logger = null;
        pluginDir = null;
    }
    
    @org.minerift.ether.debug.Debug
    public static class Debug extends Ether {
    
        public Debug(ConfigRegistry cfgs, Logger log, File pluginDir,
                Database db, NMSAccess nms, WorkQueue workQueue,
                IslandManager islands, IslandInviteManager invites,
                UserManager users) {
            super(cfgs, log, pluginDir,
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
        
        /*@Deprecated
        public void setIslandGrid(DefaultIslandGrid grid) {
            setIslandManager(new IslandManager(grid));
        }*/

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
        public File getPluginDir() {
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
        public IslandInvitesManager getIslandInvitesManager() {
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
}
