package org.minerift.ether.database.nusql;

import com.google.common.net.HostAndPort;
import org.minerift.ether.Ether;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.diff.DiffType;
import org.minerift.ether.database.diff.KeyDiff;
import org.minerift.ether.database.models.NuIslandModel;
import org.minerift.ether.database.models.NuUserModel;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandGridV2;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.user.UserManager;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static org.minerift.ether.Secrets.HIDDEN;

@SuppressWarnings("Duplicates")
public class NuSQLPlayground {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        SQLDatabaseCreationContext dbCtx = new SQLDatabaseCreationContext(SQLDialect.POSTGRES, NuIslandModel::new, NuUserModel::new);

        System.out.println("Island Model Queries:");
        dbCtx.getDMLOps().forEach(op -> System.out.println(op.getModelQuery(NuIslandModel.class)));
        System.out.println("\nUser Model Queries:");
        dbCtx.getDMLOps().forEach(op -> System.out.println(op.getModelQuery(NuUserModel.class)));

        DatabaseConnectionSettings postgresSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.POSTGRES)
                .setAddress(HostAndPort.fromHost("localhost"))
                .setDbName("ether")
                .setUsername("postgres")
                .setPassword(HIDDEN)
                .build();

        DatabaseConnectionSettings h2Settings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.H2)
                .setDbName("ether")
                .setUsername("root")
                .setPassword("")
                .build();

        DatabaseConnectionSettings sqliteSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.SQLITE)
                .setDbName("ether")
                .setUsername("root")
                .setPassword("")
                .build();

        DatabaseConnectionSettings mysqlSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.MYSQL)
                .setDbName("ether")
                .setUsername("root")
                .setPassword("password")
                .setAddress(HostAndPort.fromHost("localhost"))
                .build();

        Random random = new Random();

        final int GRID_SIZE = 100;
        IslandGridV2 grid = new IslandGridV2();
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

        try(NuSQLDatabase db = new NuSQLDatabase(h2Settings, NuIslandModel::new, NuUserModel::new)) {
            var result = db.access((access) -> {
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
    }

}
