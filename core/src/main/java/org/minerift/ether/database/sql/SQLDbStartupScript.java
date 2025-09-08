package org.minerift.ether.database.sql;

import org.minerift.ether.database.Model;

import java.util.Set;

public class SQLDbStartupScript {

    /**
     * - Main priority now should be to fix up the issues with column inconsistencies/etc.
     * - Remove old SQL API in favor of NuSQL
     * - Push changes to GitHub
     */



    public static void run(SQLAccess access) {

        // TODO: upgraders will need to run before table creation is handled

        // Create tables from models that don't exist in SQL db
        final Set<String> dbTables = access.getDatabaseTables();
        System.out.println("Tables: " + access.getDatabaseTables().toString()); // debug
        for(Model<?, ?> model : access.db().getModels()) {
            System.out.println("Testing to see if table exists: " + model.getTableName());
            if(!dbTables.contains(model.getTableName().toUpperCase())) {
                access.createTable(model);
            }
        }
        System.out.println("Tables: " + access.getDatabaseTables().toString()); // debug
        access.db().getModels().forEach(model -> System.out.println(model.getForeignFields()));


        // TODO: get rows for each table and create rows that don't exist
        // TODO: add ability to get info on rows (data types, etc.)
    }

}
