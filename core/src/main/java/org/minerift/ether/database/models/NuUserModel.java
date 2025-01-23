package org.minerift.ether.database.models;

import org.minerift.ether.database.*;
import org.minerift.ether.database.Record;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandRole;
import org.minerift.ether.user.EtherUser;

import java.util.UUID;

public class NuUserModel extends Model<EtherUser, UUID> {

    public Field<EtherUser, UUID, ?> ID;
    public Field<EtherUser, Integer, ?> ISLAND_ID;
    public Field<EtherUser, IslandRole, ?> ISLAND_ROLE;

    public NuUserModel(DatabaseCreationContext ctx) {
        super(ctx);
    }

    @Override
    protected void createModel(ModelCreationContext<Model<EtherUser, UUID>, EtherUser> ctx) {
        ctx.setTableName("users");

        NuIslandModel islandModel = ctx.getModel(NuIslandModel.class);

        ID = ctx.createField("uuid", DataType.UUID, EtherUser::getUUID);
        ISLAND_ID = ctx.createForeignField(islandModel.ISLAND_ID,
                (type) -> type.nullable(true),
                (user) -> user.getIsland().map(Island::getId).orElse(null));
        ISLAND_ROLE = ctx.createField("island_role",
                DataType.ENUM_STR(IslandRole.class).nullable(true),
                EtherUser::getIslandRole);
    }

    @Override
    public EtherUser.Builder readAsBuilder(Record<EtherUser> record) {
        var builder = EtherUser.builder()
                .setUUID(record.get(ID))
                .setIsland(record.get(ISLAND_ID))
                .setIslandRole(record.get(ISLAND_ROLE));

        return builder;
    }

    @Override
    public EtherUser readRecord(Record<EtherUser> record) {
        return readAsBuilder(record).build();
    }

    @Override
    public Field<EtherUser, UUID, ?> getPrimaryKey() {
        return ID;
    }
}
