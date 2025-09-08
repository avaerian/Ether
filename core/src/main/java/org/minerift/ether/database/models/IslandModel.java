package org.minerift.ether.database.models;

import org.minerift.ether.database.*;
import org.minerift.ether.database.Record;
import org.minerift.ether.database.sql.adapters.Adapters;
import org.minerift.ether.island.Island;

import java.util.UUID;

public class IslandModel extends Model<Island, Integer> {

    public Field<Island, Integer, ?> ISLAND_ID;
    public Field<Island, Long,    ?> COORDS;
    public Field<Island, Boolean, ?> IS_DELETED;
    public Field<Island, UUID[],  ?> MEMBERS;

    public IslandModel(DatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected void createModel(ModelCreationContext<Island> ctx) {
        ctx.setTableName("islands");
        ISLAND_ID   = ctx.createField("island_id", DataType.INT.notNull(), Island::getId);
        COORDS      = ctx.createField("coords", DataType.BIGINT.notNull(), Island::getTile, Adapters.VEC2I_2_LONG);
        IS_DELETED  = ctx.createField("is_deleted", DataType.BOOL.notNull(), Island::isDeleted);
        MEMBERS     = ctx.createField("members", DataType.UUID.array().notNull(), Island::getTeamMembers, Adapters.ETHER_USERS_2_UUIDS);
    }

    @Override
    public Island.Builder readAsBuilder(Record<Island> record) {
        var builder = Island.builder()
                .setTile(       record.get(COORDS.asComplexField()), true)
                .setMembers(    record.get(MEMBERS.asComplexField()))
                .setDeleted(    record.get(IS_DELETED))
                ;
        return builder;
    }

    @Override
    public Island readRecord(Record<Island> record) {
        return readAsBuilder(record).build();
    }

    @Override
    public Field<Island, Integer, ?> getPrimaryKey() {
        return ISLAND_ID;
    }
}
