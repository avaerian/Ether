package org.minerift.ether.database.models;

import org.jooq.Record;
import org.minerift.ether.database.*;
import org.minerift.ether.database.nusql.*;
import org.minerift.ether.database.nusql.adapters.Adapters;
import org.minerift.ether.island.Island;

import java.util.UUID;

public class NuIslandModel extends Model<Island, Integer> {

    public Field<Island, Integer, ?> ISLAND_ID;
    public Field<Island, Long,    ?> COORDS;
    public Field<Island, Boolean, ?> IS_DELETED;
    public Field<Island, UUID[],  ?> MEMBERS;

    public NuIslandModel(DatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected void createModel(ModelCreationContext<Model<Island, Integer>, Island> ctx) {
        ctx.setTableName("islands");
        ISLAND_ID   = ctx.createField("island_id", DataType.INT.notNull(), Island::getId);
        COORDS      = ctx.createField("coords", DataType.BIGINT.notNull(), Island::getTile, Adapters.VEC2I_2_LONG);
        IS_DELETED  = ctx.createField("is_deleted", DataType.BOOL.notNull(), Island::isDeleted);
        MEMBERS     = ctx.createField("members", DataType.UUIDv4.array().notNull(), Island::getTeamMembers, Adapters.ETHER_USERS_2_UUIDS);
    }

    @Override
    public Island.Builder readAsBuilder(NuSQLResult<Island> result, Record record) {
        var builder = Island.builder().setDeleted(result.readField(IS_DELETED, record));
        return builder;
    }

    @Override
    public Island readRecord(NuSQLResult<Island> result, Record record) {
        return null;
    }

    @Override
    public Field<Island, Integer, ?> getPrimaryKey() {
        return ISLAND_ID;
    }
}
