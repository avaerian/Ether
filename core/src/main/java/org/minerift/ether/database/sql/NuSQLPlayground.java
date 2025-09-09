package org.minerift.ether.database.sql;

import com.google.common.net.HostAndPort;
import org.minerift.ether.Ether;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.diff.DiffType;
import org.minerift.ether.database.diff.KeyDiff;
import org.minerift.ether.database.models.IslandModel;
import org.minerift.ether.database.models.UserModel;
import org.minerift.ether.database.sql.op.ddl.DDLGetColumns;
import org.minerift.ether.debug.Debug;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.DefaultIslandGrid;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.user.UserManager;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

//import static org.minerift.ether.Secrets.HIDDEN;

@SuppressWarnings("Duplicates")
@Debug
public class NuSQLPlayground {

    // TODO: allow for locating where database is created
    @Debug
    public static void main(String[] args) throws Exception {

        SQLDatabaseCreationContext dbCtx = new SQLDatabaseCreationContext(SQLDialect.POSTGRES, IslandModel::new, UserModel::new);

        System.out.println("Island Model Queries:");
        dbCtx.getDMLOps().forEach(op -> System.out.println(op.getModelQuery(IslandModel.class)));
        System.out.println("\nUser Model Queries:");
        dbCtx.getDMLOps().forEach(op -> System.out.println(op.getModelQuery(UserModel.class)));

        DatabaseConnectionSettings postgresSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.POSTGRES)
                .setUrl(HostAndPort.fromHost("localhost"))
                .setDbName("ether")
                .setUsername("postgres")
                /*.setPassword(HIDDEN)*/
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

        try(SQLDatabase db = new SQLDatabase(postgresSettings, IslandModel::new, UserModel::new)) {
            db.getModels().forEach(model -> System.out.println(model.getTableName() + " : " + model.getForeignFields()));

            var result = db.access((access) -> {

                access.selectAll(UserModel.class).streamField(db.getModel(UserModel.class).ISLAND_ROLE).forEach(System.out::println);

                Set<Integer> oldIslandIds = access.selectAllIds(IslandModel.class).collect(Collectors.toSet());
                Set<Integer> newIslandIds = grid.getIslandsView().stream().map(Island::getId).collect(Collectors.toSet());
                var islands_diffs = KeyDiff.partitionDiffs(oldIslandIds, newIslandIds);

                var islands_inserted = islands_diffs.getOrDefault(DiffType.INSERTED, Collections.emptyList());
                var islands_updated = islands_diffs.getOrDefault(DiffType.UPDATED, Collections.emptyList());
                var islands_deleted = islands_diffs.getOrDefault(DiffType.DELETED, Collections.emptyList());

                System.out.println("Inserted: " + islands_inserted);
                System.out.println("Updated: " + islands_updated);
                System.out.println("Deleted: " + islands_deleted);

                var users_diffs = KeyDiff.partitionDiffs(access.selectAllIds(UserModel.class).collect(Collectors.toSet()), users.getKeySet());
                var users_inserted = users_diffs.getOrDefault(DiffType.INSERTED, Collections.emptyList());
                var users_updated = users_diffs.getOrDefault(DiffType.UPDATED, Collections.emptyList());
                var users_deleted = users_diffs.getOrDefault(DiffType.DELETED, Collections.emptyList());

                System.out.println("Inserted: " + users_inserted);
                System.out.println("Updated: " + users_updated);
                System.out.println("Deleted: " + users_deleted);

                access.insert(IslandModel.class, Ether.getIslandManager().getIslands(islands_inserted));
                access.update(IslandModel.class, Ether.getIslandManager().getIslands(islands_updated));
                access.deleteByIds(IslandModel.class, islands_deleted);
                access.commit();
                System.out.println("Committed!");

                access.insert(UserModel.class, Ether.getUserManager().getUsers(users_inserted));
                access.update(UserModel.class, Ether.getUserManager().getUsers(users_updated));
                access.deleteByIds(UserModel.class, users_deleted);
                access.commit();
                System.out.println("Committed!");
            });

            for(int i = 0; i < 10; i++) {
                System.out.println("Hello, world!");
                Thread.sleep(Duration.ofMillis(25).toMillis());
            }

            result.get();
        }

        System.out.println("\nPostgreSQL");
        Database db = new SQLDatabase(postgresSettings, IslandModel::new, UserModel::new);
        db.accessSync(access -> {
            access.db().getModels().forEach(model -> DDLGetColumns.getTableColumns((SQLAccess) access, model));
        });
        db.close();

        System.out.println("\nH2");
        db = new SQLDatabase(h2Settings, IslandModel::new, UserModel::new);
        db.accessSync(access -> {
            access.db().getModels().forEach(model -> DDLGetColumns.getTableColumns((SQLAccess) access, model));
        });
        db.close();

        System.out.println("\nSQLite");
        db = new SQLDatabase(sqliteSettings, IslandModel::new, UserModel::new);
        db.accessSync(access -> {
            access.db().getModels().forEach(model -> DDLGetColumns.getTableColumns((SQLAccess) access, model));

            //((NuSQLAccess)access).dsl().resultQuery("PRAGMA function_list;").fetchStream().forEach(System.out::println);
        });
        db.close();

        System.out.println("\nMySQL");
        db = new SQLDatabase(mysqlSettings, IslandModel::new, UserModel::new);
        db.accessSync(access -> {
            access.db().getModels().forEach(model -> DDLGetColumns.getTableColumns((SQLAccess) access, model));
        });
        db.close();
    }

}
