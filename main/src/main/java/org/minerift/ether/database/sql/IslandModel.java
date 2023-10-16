package org.minerift.ether.database.sql;

import org.jooq.impl.SQLDataType;
import org.minerift.ether.database.sql.adapters.Adapters;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.database.sql.model.PrimaryKey;
import org.minerift.ether.island.Island;

import java.util.UUID;

public class IslandModel extends Model<Island> {

    public static final IslandModel ISLAND_MODEL = new IslandModel();

    public final @PrimaryKey Field<Island, Integer> ID  = createField("island_id", SQLDataType.INTEGER.notNull(), Island::getId);
    public final Field<Island, Boolean> IS_DELETED      = createField("is_deleted", SQLDataType.BIT.notNull(), Island::isDeleted);

    public final Field<Island, UUID[]> MEMBERS          = createField("members", SQLDataType.UUID.array(), Island::getTeamMembers, Adapters.ETHER_USERS_2_UUIDS);

    private IslandModel() {
        super("islands");
    }

    @Override
    public Field<Island, ?>[] fields() {
        return new Field[] {
                ID,
                IS_DELETED,
                MEMBERS,

        };
    }
}
