package org.minerift.ether.database.nusql;

import org.minerift.ether.database.models.NuIslandModel;
import org.minerift.ether.database.models.NuUserModel;

public class NuSQLPlayground {

    public static void main(String[] args) {

        SQLDatabaseCreationContext dbCtx = new SQLDatabaseCreationContext(SQLDialect.POSTGRES, NuIslandModel::new, NuUserModel::new);

        System.out.println("Island Model Queries:");
        dbCtx.getDMLOps().forEach(op -> System.out.println(op.getModelQuery(NuIslandModel.class)));
        System.out.println("\nUser Model Queries:");
        dbCtx.getDMLOps().forEach(op -> System.out.println(op.getModelQuery(NuUserModel.class)));
    }

}
