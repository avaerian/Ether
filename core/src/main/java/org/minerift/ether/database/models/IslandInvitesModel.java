package org.minerift.ether.database.models;

import org.minerift.ether.database.*;
import org.minerift.ether.database.Record;
import org.minerift.ether.island.invites.IslandInvite;
import org.minerift.ether.util.fn.IBuilder;

import java.util.UUID;

public class IslandInvitesModel extends Model<IslandInvite, Integer> {

    public Field<IslandInvite, Integer, ?> INVITE_ID;
    public Field<IslandInvite, UUID, ?> SENDER;
    public Field<IslandInvite, UUID, ?> RECEIVER;
    public Field<IslandInvite, Long, ?> EXPIRE_TIMESTAMP;
    public Field<IslandInvite, Integer, ?> ISLAND_ID;

    public IslandInvitesModel(DatabaseCreationContext dbCtx) {
        super(dbCtx);
    }


    /*@Override
    public IslandInvite readResult(SQLResult<IslandInvite> result, Record record) {
        UUIDPair senderReceiver = result.readField(SENDER_RECEIVER.asComplexField(), record);
        long expire = result.readField(EXPIRE_TIMESTAMP, record);

        int islandId = result.readField(ISLAND_ID, record);
        Island island = Ether.getIslandManager().getIslandAt(islandId).orElse(null);
        if(island == null) {
            Ether.getLogger().warning("Unable to find island id " + islandId + " for island invite.");
            return null; // island invite is invalid for unknown islands // TODO: nulls should be ignored when reading this result (when reading into collection)
        }

        //return new IslandInvite(senderReceiver, island, expire);
        return IslandInvite.create(senderReceiver, island, expire);
    }*/

    @Override
    protected void createModel(ModelCreationContext<IslandInvite> ctx) {
        ctx.setTableName("island_invites");

        IslandModel islandModel = ctx.getModel(IslandModel.class);

        INVITE_ID = ctx.createField("invite_id", DataType.INT, IslandInvite::getInviteId); // TODO: temporary until refactor to UUIDv7
        SENDER = ctx.createField("sender", DataType.UUID, IslandInvite::getSender);
        RECEIVER = ctx.createField("receiver", DataType.UUID, IslandInvite::getReceiver);
        EXPIRE_TIMESTAMP = ctx.createField("expire", DataType.LONG.notNull(), IslandInvite::getExpireTimestamp);
        ISLAND_ID = ctx.createForeignField(islandModel.ISLAND_ID, (invite) -> invite.getIsland().get().getId());
    }

    @Override
    public IBuilder<IslandInvite> readAsBuilder(Record<IslandInvite> record) {
        throw new UnsupportedOperationException("No builder for IslandInvite");
    }

    @Override
    public IslandInvite readRecord(Record<IslandInvite> record) {
        return null;
    }

    @Override
    public Field<IslandInvite, Integer, ?> getPrimaryKey() {
        return INVITE_ID;
    }
}
