package org.minerift.ether.database.nusql;

import lombok.Getter;
import org.minerift.ether.database.Database;
import org.minerift.ether.database.DatabaseConnectionSettings;
import org.minerift.ether.database.DatabaseException;
import org.minerift.ether.database.Model;
import org.minerift.ether.database.nusql.query.dml.*;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

@Getter
public class NuSQLDatabase extends Database {

    protected DataSource dataSource;
    protected List<Model<?,?>> models;
    protected DMLInsert insert;
    protected DMLUpdate update;
    protected DMLDelete delete;
    protected DMLSelect select;

    public NuSQLDatabase(DatabaseConnectionSettings settings, Function<NuSQLDatabase, Model<?,?>>... initModels) {
        super(settings.dbName());

        CompletableFuture<DataSource> connect = CompletableFuture.supplyAsync(() ->
                settings.dialect().getNuSQLConnector().connect(this, settings));

        insert = new DMLInsert();
        update = new DMLUpdate();
        delete = new DMLDelete();
        select = new DMLSelect();

        List<Model<?,?>> models = new ArrayList<>(initModels.length);
        for(var init : initModels) {
            models.add(init.apply(this));
        }
        this.models = models;

        dataSource = connect.join();
    }

    @Override
    public CompletableFuture<DatabaseException> access(boolean autocommit, boolean async, DbAccessFunction proc) {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
