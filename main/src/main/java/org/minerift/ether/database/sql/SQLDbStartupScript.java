package org.minerift.ether.database.sql;

import org.minerift.ether.database.sql.model.Model;

import java.util.Set;

public class SQLDbStartupScript {

    public static void run(SQLAccess access) {

        // TODO: upgraders will need to run before table creation is handled

        // Create tables that don't exist
        final Set<String> dbTables = access.getDatabaseTables();
        System.out.println("Tables: " + access.getDatabaseTables().toString()); // debug
        for(Model<?, ?> model : access.db().getModels()) {
            System.out.println("Testing to see if table exists: " + model.TABLE_NAME);
            if(!dbTables.contains(model.TABLE_NAME.toUpperCase())) {
                access.createTable(model);
            }
        }
        System.out.println("Tables: " + access.getDatabaseTables().toString()); // debug
    }

}
