package org.minerift.ether.database.sql.metadata;

import org.jooq.Record;
import org.jooq.impl.SQLDataType;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.SQLResult;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;

// Stores database metadata and global session info
public class MetadataModel extends Model<Metadata, Integer> {

    // Id should always be 0 to ensure single-entry table
    public final Field<Metadata, Integer, ?> ID             = createField("id", SQLDataType.INTEGER, (ignore) -> 0);
    public final Field<Metadata, Integer, ?> DB_VERSION     = createField("db_version", SQLDataType.INTEGER, Metadata::getDbVersion);


    public MetadataModel(SQLDatabase db) {
        super("metadata", db);
        setupFields(ID, DB_VERSION);
    }

    @Override
    public Field<Metadata, Integer, ?> getPrimaryKey() {
        return ID;
    }

    @Override
    public Metadata readResult(SQLResult<Metadata> result, Record record) {
        int version = result.getField(DB_VERSION, record);
        return new Metadata(version);
    }
}
