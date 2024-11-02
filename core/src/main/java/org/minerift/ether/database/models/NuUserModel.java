package org.minerift.ether.database.models;

import org.jooq.Record;
import org.minerift.ether.database.*;
import org.minerift.ether.database.nusql.*;
import org.minerift.ether.island.Island;
import org.minerift.ether.user.EtherUser;

import java.util.UUID;

public class NuUserModel extends Model<EtherUser, UUID> {

    public Field<EtherUser, UUID, ?> ID;
    public Field<EtherUser, Integer, ?> ISLAND_ID;
    public Field<EtherUser, String, ?> ISLAND_ROLE;

    public NuUserModel(DatabaseCreationContext ctx) {
        super(ctx);
    }

    @Override
    protected void createModel(ModelCreationContext<Model<EtherUser, UUID>, EtherUser> ctx) {
        ctx.setTableName("users");

        NuIslandModel islandModel = ctx.getModel(NuIslandModel.class);

        ID = ctx.createField("uuid", DataType.UUIDv4, EtherUser::getUUID);
        ISLAND_ID = ctx.createForeignField(islandModel.ISLAND_ID, // TODO: add Function<DataType<T>, DataType<T>> that allows us to add additional flags to type
                (user) -> user.getIsland().map(Island::getId).orElse(null));
        ISLAND_ROLE = ctx.createField("island_role", DataType.VARCHAR(16).nullable(true),
                (user) -> user.getIslandRole().toString());

    }

    @Override
    public EtherUser.Builder readAsBuilder(NuSQLResult<EtherUser> result, Record record) {
        return null;
    }

    @Override
    public EtherUser readRecord(NuSQLResult<EtherUser> result, Record record) {
        return null;
    }

    @Override
    public Field<EtherUser, UUID, ?> getPrimaryKey() {
        return ID;
    }
}
