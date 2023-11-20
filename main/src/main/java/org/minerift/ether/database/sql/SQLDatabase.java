package org.minerift.ether.database.sql;

import com.google.common.net.HostAndPort;
import com.zaxxer.hikari.HikariDataSource;
import org.minerift.ether.database.sql.connect.SQLConnector;
import org.minerift.ether.database.sql.metadata.MetadataModel;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.operations.dml.*;
import org.minerift.ether.database.sql.operations.dml.bind.NamedBindValues;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandGrid;
import org.minerift.ether.math.GridAlgorithm;
import org.minerift.ether.user.EtherUser;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Collection;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;

import static org.minerift.ether.database.sql.DatabasePlayground.HIDDEN;

public class SQLDatabase implements AutoCloseable {


    public static void main(String[] args) throws Exception {

        // To connect to MySQL:
        //  - try connect with db name
        //  - if fails, try connect without db name and create db
        //  - if fails, throw database connection exception (db not online)

        // To connect to PostgreSQL:
        //  - try connect with db name
        //      - if fails, try connect without db name and create db
        //          - if fails, throw database connection exception (db not online)
        //          - close connection and try connect with db name
        //          - if fails, throw database connection exception (created db but failed to connect)
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

        SQLDatabase db = new SQLDatabase(sqliteSettings, IslandModel::new);
        SQLContext ctx = db.getSQLContext();

        IslandModel model = db.getTable(IslandModel.class);

        // TODO: remove create DB query and move to connect() method for SQLConnector's
        //ctx.createDbQuery.getQuery(db).execute();
        ctx.createTableQuery.getQuery(model).execute();

        Random random = new Random();

        final int GRID_SIZE = 100;
        IslandGrid grid = new IslandGrid();
        for(int i = 0; i < GRID_SIZE; i++) {
            final Island island = Island.builder()
                    .setTile(GridAlgorithm.computeTile(i), true)
                    .setOwner(EtherUser.builder().setUUID(UUID.randomUUID()).build())
                    .setDeleted(random.nextBoolean())
                    .build();
            grid.registerIsland(island);
        }

        var islandsView = grid.getIslandsView();

        var islandsDao  = new Dao<>(IslandModel.class,      IslandModel::getPrimaryKey,     ctx);
        var metadataDao = new Dao<>(MetadataModel.class,    MetadataModel::getPrimaryKey,   ctx);

        islandsDao.insertOrUpdate(islandsView.get(0));

        /*
        var upsertQuery = insertOrUpdate.getQuery(model);
        DMLModelBatch<Island> batch = new DMLModelBatch<>(ctx, model, upsertQuery.right(), upsertQuery.left().getBindOrder());
        batch.bindAll(islandsView);
        batch.execute();
         */

        ctx.upsertQuery.queryFor(model).bind(model.dumpNamedBindValues_New(islandsView.get(0))).execute();
        //insertOrUpdate.getBatch(model).bindAll(islandsView).execute();

        System.out.println(ctx.selectQuery.queryFor(model).bind(NamedBindValues.of(model.ID, 10)).fetch());

        System.out.println(ctx.selectAllQuery.queryFor(model).fetch());

        db.close();
    }

    private final HikariDataSource dataSource;
    private final SQLContext ctx;
    private final String dbName;

    public SQLDatabase(DatabaseConnectionSettings settings, Function<SQLContext, Model<?>> ... tables) {
        // TODO: better error handling here???
        SQLConnector connector = settings.getDialect().getDbConnector();
        this.dataSource = new HikariDataSource(connector.createConfig(settings));

        try {
            if (!dataSource.getConnection().isValid(10)) { // 10 seconds
                throw new SQLTimeoutException("Unable to connect to database; invalid connection.");
            }

            System.out.println("Database connection has been established!");
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }

        this.dbName = settings.getDbName();
        this.ctx = new SQLContext(this, settings.getDialect(), tables);
    }

    public String getDbName() {
        return dbName;
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    public SQLContext getSQLContext() {
        return ctx;
    }

    public <M extends Model> M getTable(Class<M> modelClazz) {
        return ctx.getTable(modelClazz);
    }

    public Collection<Model<?>> getTables() {
        return ctx.getTables();
    }

    @Override
    public void close() {
        dataSource.close();
    }
}
