package org.minerift.ether.database.nusql;

import com.google.common.net.HostAndPort;
import org.minerift.ether.Ether;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.DatabaseCreationContext;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.diff.DiffType;
import org.minerift.ether.database.diff.KeyDiff;
import org.minerift.ether.database.models.NuIslandModel;
import org.minerift.ether.database.models.NuUserModel;
import org.minerift.ether.database.nusql.op.ddl.DDLGetColumns;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.DefaultIslandGrid;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.user.UserManager;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.minerift.ether.Secrets.HIDDEN;

@SuppressWarnings("Duplicates")
@Debug
public class NuSQLPlayground {

    public static void main(String[] args) throws Exception {

        SQLDatabaseCreationContext dbCtx = new SQLDatabaseCreationContext(SQLDialect.POSTGRES, NuIslandModel::new, NuUserModel::new);

        System.out.println("Island Model Queries:");
        dbCtx.getDMLOps().forEach(op -> System.out.println(op.getModelQuery(NuIslandModel.class)));
        System.out.println("\nUser Model Queries:");
        dbCtx.getDMLOps().forEach(op -> System.out.println(op.getModelQuery(NuUserModel.class)));

        DatabaseConnectionSettings postgresSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.POSTGRES)
                .setUrl(HostAndPort.fromHost("localhost"))
                .setDbName("ether")
                .setUsername("postgres")
                .setPassword(HIDDEN)
                .build();

        DatabaseConnectionSettings h2Settings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.H2)
                .setUrl("C:\\Users\\avaer\\Desktop\\")
                .setDbName("ether")
                .setUsername("root")
                .setPassword("")
                .build();

        DatabaseConnectionSettings sqliteSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.SQLITE)
                .setUrl("C:\\Users/avaer\\Desktop/")
                .setDbName("ether")
                .setUsername("root")
                .setPassword("")
                .build();

        DatabaseConnectionSettings mysqlSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.MYSQL)
                .setDbName("ether")
                .setUsername("root")
                .setPassword("password")
                .setUrl(HostAndPort.fromHost("localhost"))
                .build();

        Random random = new Random();

        final int GRID_SIZE = 100;
        DefaultIslandGrid grid = new DefaultIslandGrid();
        UserManager users = new UserManager();
        for(int i = 0; i < GRID_SIZE; i++) {
            final EtherUser user = EtherUser.builder().setUUID(UUID.randomUUID()).build();
            users.register(user);
            final Island island = Island.builder()
                    .setTile(GridAlgorithm.computeTile(i), true)
                    .setOwner(user)
                    .setDeleted(random.nextBoolean())
                    .build();
            grid.registerIsland(island);
        }

        Ether.Debug.setIslandGrid(grid);
        Ether.Debug.setUserManager(users);

        try(NuSQLDatabase db = new NuSQLDatabase(postgresSettings, NuIslandModel::new, NuUserModel::new)) {
            db.getModels().forEach(model -> System.out.println(model.getTableName() + " : " + model.getForeignFields()));

            var result = db.access((access) -> {

                access.selectAll(NuUserModel.class).streamField(db.getModel(NuUserModel.class).ISLAND_ROLE).forEach(System.out::println);

                Set<Integer> oldIslandIds = access.selectAllIds(NuIslandModel.class).collect(Collectors.toSet());
                Set<Integer> newIslandIds = grid.getIslandsView().stream().map(Island::getId).collect(Collectors.toSet());
                var islands_diffs = KeyDiff.partitionDiffs(oldIslandIds, newIslandIds);

                var islands_inserted = islands_diffs.getOrDefault(DiffType.INSERTED, Collections.emptyList());
                var islands_updated = islands_diffs.getOrDefault(DiffType.UPDATED, Collections.emptyList());
                var islands_deleted = islands_diffs.getOrDefault(DiffType.DELETED, Collections.emptyList());

                System.out.println("Inserted: " + islands_inserted);
                System.out.println("Updated: " + islands_updated);
                System.out.println("Deleted: " + islands_deleted);

                var users_diffs = KeyDiff.partitionDiffs(access.selectAllIds(NuUserModel.class).collect(Collectors.toSet()), users.getKeySet());
                var users_inserted = users_diffs.getOrDefault(DiffType.INSERTED, Collections.emptyList());
                var users_updated = users_diffs.getOrDefault(DiffType.UPDATED, Collections.emptyList());
                var users_deleted = users_diffs.getOrDefault(DiffType.DELETED, Collections.emptyList());

                System.out.println("Inserted: " + users_inserted);
                System.out.println("Updated: " + users_updated);
                System.out.println("Deleted: " + users_deleted);

                access.insert(NuIslandModel.class, Ether.getIslandManager().getIslands(islands_inserted));
                access.update(NuIslandModel.class, Ether.getIslandManager().getIslands(islands_updated));
                access.deleteByIds(NuIslandModel.class, islands_deleted);
                access.commit();
                System.out.println("Committed!");

                access.insert(NuUserModel.class, Ether.getUserManager().getUsers(users_inserted));
                access.update(NuUserModel.class, Ether.getUserManager().getUsers(users_updated));
                access.deleteByIds(NuUserModel.class, users_deleted);
                access.commit();
                System.out.println("Committed!");
            });

            for(int i = 0; i < 10; i++) {
                System.out.println("Hello, world!");
                Thread.sleep(Duration.ofMillis(25));
            }

            result.get();
        }

        System.out.println("\nPostgreSQL");
        Database db = new NuSQLDatabase(postgresSettings, NuIslandModel::new, NuUserModel::new);
        db.accessSync(access -> {
            access.db().getModels().forEach(model -> DDLGetColumns.getTableColumns((NuSQLAccess) access, model));
        });
        db.close();

        System.out.println("\nH2");
        db = new NuSQLDatabase(h2Settings, NuIslandModel::new, NuUserModel::new);
        db.accessSync(access -> {
            access.db().getModels().forEach(model -> DDLGetColumns.getTableColumns((NuSQLAccess) access, model));
        });
        db.close();

        System.out.println("\nSQLite");
        db = new NuSQLDatabase(sqliteSettings, NuIslandModel::new, NuUserModel::new);
        db.accessSync(access -> {
            access.db().getModels().forEach(model -> DDLGetColumns.getTableColumns((NuSQLAccess) access, model));

            //((NuSQLAccess)access).dsl().resultQuery("PRAGMA function_list;").fetchStream().forEach(System.out::println);
        });
        db.close();

        System.out.println("\nMySQL");
        db = new NuSQLDatabase(mysqlSettings, NuIslandModel::new, NuUserModel::new);
        db.accessSync(access -> {
            access.db().getModels().forEach(model -> DDLGetColumns.getTableColumns((NuSQLAccess) access, model));
        });
        db.close();
    }

}
