package org.minerift.ether.database.models;

import org.minerift.ether.database.Record;
import org.minerift.ether.database.*;
import org.minerift.ether.util.fn.IBuilder;

// Stores database metadata and global session info
public class MetadataModel extends Model<Metadata, Integer> {

    // Id should always be 0 to ensure single-entry table
    public Field<Metadata, Integer, ?> ID;
    public Field<Metadata, Integer, ?> DB_VERSION;

    public MetadataModel(DatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected void createModel(ModelCreationContext<Model<Metadata, Integer>, Metadata> ctx) {
        ctx.setTableName("metadata");

        ID = ctx.createField("id", DataType.INT, (ignore) -> 0); // TODO: switch to byte?
        DB_VERSION = ctx.createField("db_version", DataType.INT.notNull(), Metadata::getDbVersion);
    }

    @Deprecated
    @Override
    public IBuilder<Metadata> readAsBuilder(Record<Metadata> record) {
        throw new UnsupportedOperationException("No builder for metadata");
    }

    @Override
    public Metadata readRecord(Record<Metadata> record) {
        return null;
    }

    @Override
    public Field<Metadata, Integer, ?> getPrimaryKey() {
        return ID;
    }
}
