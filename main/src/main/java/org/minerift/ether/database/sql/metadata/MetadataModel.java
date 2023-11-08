package org.minerift.ether.database.sql.metadata;

import org.jooq.impl.SQLDataType;
import org.minerift.ether.database.sql.SQLContext;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.model.PrimaryKey;

// Stores database metadata and global session info
public class MetadataModel extends Model<Metadata> {

    // Id should always be 0 to ensure single-entry table
    public final @PrimaryKey Field<Metadata, Integer, ?> ID = createField("id", SQLDataType.INTEGER, (ignore) -> 0);
    public final Field<Metadata, Integer, ?> DB_VERSION     = createField("db_version", SQLDataType.INTEGER, Metadata::getDbVersion);


    public MetadataModel(SQLContext ctx) {
        super("metadata", ctx);
    }

    @Override
    public Field<Metadata, ?, ?>[] fields() {
        return new Field[]{ ID, DB_VERSION };
    }
}
