package org.minerift.ether.database.nusql;

import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.Field;
import org.minerift.ether.database.models.IslandInvitesModel;
import org.minerift.ether.database.models.IslandModel;
import org.minerift.ether.database.models.UserModel;
import org.minerift.ether.database.nusql.query.cond.Condition;
import org.minerift.ether.database.nusql.query.dml.DMLDelete;
import org.minerift.ether.database.nusql.query.dml.DMLInsert;
import org.minerift.ether.database.nusql.query.dml.DMLSelect;
import org.minerift.ether.database.nusql.query.dml.DMLUpdate;
import org.minerift.ether.database.sql.SQLDatabaseCreationContext;
import org.minerift.ether.database.sql.SQLDialect;
import org.minerift.ether.debug.Debug;

import static org.minerift.ether.database.nusql.query.cond.Condition.eq;
import static org.minerift.ether.database.nusql.query.cond.Condition.gt;

@Debug
public class NuSQLPlayground {

    @Debug
    public static void main(String[] args) {

        SQLDatabaseCreationContext dbCtx = new SQLDatabaseCreationContext(SQLDialect.POSTGRES, IslandModel::new, IslandInvitesModel::new, UserModel::new);

        System.out.println("Island Model Queries:");
        dbCtx.getDMLOps().forEach(op -> System.out.println(op.getModelQuery(IslandModel.class)));
        System.out.println("\nUser Model Queries:");
        dbCtx.getDMLOps().forEach(op -> System.out.println(op.getModelQuery(UserModel.class)));

        System.out.println("\n\nNew query experiment:");
        dbCtx.getModels().forEach(model -> System.out.println(DMLInsert.insertQuery(model)));
        dbCtx.getModels().forEach(model -> System.out.println(DMLUpdate.updateQuery(model)));
        dbCtx.getModels().forEach(model -> System.out.println(DMLDelete.deleteQuery(model)));
        dbCtx.getModels().forEach(model -> {
            Field<?, ?> pk = model.getPrimaryKey();
            Condition cond = eq(pk)
                            .and( gt((Field<?, Comparable>)pk).or( eq(pk) ).and( eq(pk) ) )
                            .and( eq(pk).or( eq(pk).and(eq(pk).or(eq(pk))) ) )
                            .or( eq(pk) );

            System.out.println("\nCONDITION: " + cond.root() + '\n');

            //TODO: System.out.println("cond=");
            System.out.println(DMLSelect.selectQuery(model, cond));
        });


        DatabaseConnectionSettings postgresSettings = DatabaseConnectionSettings.builder()
                .setDialect(SQLDialect.POSTGRES)
                .setUrl("localhost")
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
                .setUrl("localhost")
                .build();

    }

}
