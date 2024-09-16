package org.minerift.ether.database.sql;

import org.jooq.Record;
import org.jooq.impl.SQLDataType;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.island.Island;
import org.minerift.ether.user.EtherUser;

import java.util.Optional;
import java.util.UUID;

public class UserModel extends Model<EtherUser, UUID> {

    public final Field<EtherUser, UUID, ?> USER_ID = createField("user_id", SQLDataType.UUID.notNull(), EtherUser::getUUID);
    public final Field<EtherUser, Integer, ?>   ISLAND_ID = createField("island_id", SQLDataType.INTEGER.nullable(true), (user) -> {
        // read island id if present, else null
        Optional<Island> optIsland = user.getIsland();
        return optIsland.isPresent() ? optIsland.get().getId() : null;
    });

    public final Field<EtherUser, String, ?>    ISLAND_ROLE = createField("island_role", SQLDataType.VARCHAR, (user) -> user.getIslandRole().name());

    public UserModel(SQLDatabase db) {
        super("users", db);
        registerFields();
    }

    @Override
    public EtherUser readResult(SQLResult<EtherUser> result, Record record) {
        return readAsBuilder(result, record).build();
    }

    @Override
    public EtherUser.Builder readAsBuilder(SQLResult<EtherUser> result, Record record) {
        Integer islandId = result.readField(ISLAND_ID, record);
        return EtherUser.builder()
                .setUUID(result.readField(USER_ID, record))
                //.setIsland(islandId != null ? Ether.getIslandManager().getIslandAt(GridAlgorithm.computeTile(islandId)).get() : null)

                ;
    }

    @Override
    public Field<EtherUser, UUID, ?> getPrimaryKey() {
        return USER_ID;
    }
}
