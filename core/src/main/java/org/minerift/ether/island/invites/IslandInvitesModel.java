package org.minerift.ether.island.invites;

import org.jooq.Record;
import org.jooq.impl.SQLDataType;
import org.minerift.ether.Ether;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.nusql.SQLDialect;
import org.minerift.ether.database.sql.SQLResult;
import org.minerift.ether.database.nusql.adapters.Adapters;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.island.Island;
import org.minerift.ether.util.IBuilder;
import org.minerift.ether.util.pair.UUIDPair;

import java.util.UUID;

public class IslandInvitesModel extends Model<IslandInvite, UUID[]> {

    public final Field<IslandInvite, UUID[], ?>     SENDER_RECEIVER = createField("sender_receiver", SQLDataType.UUID.array().notNull(), IslandInvite::getSenderReceiver, Adapters.PAIR_2_UUIDS);
    public final Field<IslandInvite, Long, ?>       EXPIRE_TIMESTAMP = createField("expire", SQLDataType.BIGINT.notNull(), IslandInvite::getExpireTimestamp);
    public final Field<IslandInvite, Integer, ?>    ISLAND_ID = createField("island_id", SQLDataType.INTEGER.notNull(), (invite) -> invite.getIsland().getId());

    public static void main(String[] args) {
        new IslandInvitesModel(SQLDatabase.debug(SQLDialect.H2));
    }

    public IslandInvitesModel(SQLDatabase db) {
        super("island_invites", db);
        registerFields();
    }

    @Override
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
    }

    @Override
    public IBuilder<IslandInvite> readAsBuilder(SQLResult<IslandInvite> result, Record record) {
        throw new UnsupportedOperationException("No builder for IslandInvite!");
    }

    @Override
    public Field<IslandInvite, UUID[], ?> getPrimaryKey() {
        return SENDER_RECEIVER;
    }
}
