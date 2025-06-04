package org.minerift.ether.database.nubin;

import org.minerift.ether.database.Database;
import org.minerift.ether.database.DatabaseException;

import java.util.concurrent.CompletableFuture;

// TODO: create BinaryStorage class for local binary database handling
public class BinaryDatabase extends Database {

    public BinaryDatabase(String dbName) {
        super(dbName);
    }

    @Override
    public CompletableFuture<DatabaseException> access(boolean autocommit, boolean async, DbAccessFunction proc) {
        return null;
    }

    @Override
    public void close() {

    }
}
