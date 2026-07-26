package org.minerift.ether.database.models;

import org.minerift.ether.database.*;
import org.minerift.ether.database.Record;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandRole;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.UnreachableException;

import java.util.UUID;

import static org.minerift.ether.island.Island.INVALID_ID;

public class UserModel extends Model<EtherUser, UUID> {

    public Field<EtherUser, UUID, ?> ID;
    public Field<EtherUser, Integer, ?> ISLAND_ID;
    public Field<EtherUser, IslandRole, ?> ISLAND_ROLE;

    public UserModel(DatabaseCreationContext ctx) {
        super(ctx);
    }

    @Override
    protected void createModel(ModelCreationContext<EtherUser> ctx) {
        ctx.setTableName("users");

        IslandModel islandModel = ctx.getModel(IslandModel.class);

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
        Integer island = record.get(ISLAND_ID);
        EtherUser.Builder builder = EtherUser.builder()
                .setUUID(record.get(ID))
                .setIsland(island != null ? island : INVALID_ID)
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
