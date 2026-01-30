package org.minerift.ether.database.models;

import org.minerift.ether.Ether;
import org.minerift.ether.database.*;
import org.minerift.ether.database.Record;
import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.world.ChunkCoords;

import java.util.Collections;

public class ServerDataModel extends Model<ServerData, Integer> {

    public Field<ServerData, Integer, ?> ID;
    public Field<ServerData, Long[], ?> TILE_COORDS;

    public ServerDataModel(DatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected void createModel(ModelCreationContext<ServerData> ctx) {
        ctx.setTableName("server_data");
        ID = ctx.createField("id", DataType.INT, (__) -> 0);
        TILE_COORDS = ctx.createField("tile_coords", DataType.LONG.array(),
                (data) -> Collections.unmodifiableCollection(data.purgeQueue)
                        .stream().map(ChunkCoords::getChunkKey).toArray(Long[]::new));
    }

    @Override
    public IBuilder<ServerData> readAsBuilder(Record<ServerData> record) {
        return null;
    }

    @Override
    public ServerData readRecord(Record<ServerData> record) {
        return null;
    }

    @Override
    public Field<ServerData, Integer, ?> getPrimaryKey() {
        return ID;
    }

}
