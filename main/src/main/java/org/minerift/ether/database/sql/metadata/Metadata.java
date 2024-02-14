package org.minerift.ether.database.sql.metadata;

public class Metadata {

    // Represents the version of the database schemas
    // If any updates are made to tables, the version can be read
    // and the data can go through a list of upgraders.
    private int dbVersion = 1;

    public Metadata(int dbVersion) {
        this.dbVersion = dbVersion;
    }

    public int getDbVersion() {
        return dbVersion;
    }

    public void setDbVersion(int dbVersion) {
        this.dbVersion = dbVersion;
    }
}
