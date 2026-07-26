package org.minerift.ether.database.models;

import org.minerift.ether.database.*;
import org.minerift.ether.database.Record;
import org.minerift.ether.database.adapters.Adapters;
import org.minerift.ether.island.Island;
import org.minerift.ether.island.IslandValue;
import org.minerift.ether.island.PermissionSet;
import org.minerift.ether.nms.world.block.BlockState;

import java.util.Arrays;
import java.util.UUID;

public class IslandModel extends Model<Island, Integer> {

    public Field<Island, Integer, ?> ISLAND_ID;
    public Field<Island, Long,    ?> COORDS;
    public Field<Island, UUID, ?> OWNER;
    public Field<Island, UUID[],  ?> MEMBERS;
    public Field<Island, long[], ?> PERM_SET;
    public Field<Island, int[], ?> POWER_BY_BLOCK; // WORTH
    public Field<Island, String[], ?> BLOCK_NAMES; // WORTH

    public IslandModel(DatabaseCreationContext dbCtx) {
        super(dbCtx);
    }

    @Override
    protected void createModel(ModelCreationContext<Island> ctx) {
        ctx.setTableName("islands");
        ISLAND_ID   = ctx.createField("island_id", DataType.INT.notNull(), Island::getId);
        COORDS      = ctx.createField("coords", DataType.LONG.notNull(), Island::getTile, Adapters.VEC2I_2_LONG);
        OWNER       = ctx.createField("owner", DataType.UUID.notNull(), (is) -> is.getOwner().getUUID());
        MEMBERS     = ctx.createField("members", DataType.UUID.array().notNull(), Island::getTeamMembers, Adapters.ETHER_USERS_2_UUIDS);
        PERM_SET    = ctx.createField("perm_set", DataType.LONGS.notNull(), (is) -> is.getPermissions().getPermsMut());
        /* WORTH */ POWER_BY_BLOCK = ctx.createField("power_by_block", DataType.INTS, (is) -> is.getValue().getValues());
        /* WORTH */ BLOCK_NAMES = ctx.createField("block_names", DataType.VARCHAR.array(),
                (is) -> Arrays.stream(is.getValue().getBlockStates())
                        .map(BlockState::getAsString).toArray(String[]::new));
    }

    @Override
    public Island.Builder readAsBuilder(Record<Island> record) {
        // get island worth
        final int[] powerByBlock = record.get(POWER_BY_BLOCK);
        final String[] blockNames = record.get(BLOCK_NAMES);
        final IslandValue.Mutable worth = new IslandValue.Mutable();
        for(int i = 0; i < blockNames.length; i++) {
            BlockState state = BlockState.of(blockNames[i], null);
            if(state != null) { // TODO: fixer-upper upgrader
                worth.add(state, powerByBlock[i]);
            }
        }

        return Island.builder()
                .setTile(record.get(COORDS.asComplexField()), true)
                .setOwner(record.get(OWNER))
                .setMembers(record.get(MEMBERS.asComplexField()))
                .setWorth(worth)
                .setPermissionSet(new PermissionSet(record.get(PERM_SET)))

                ;
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
