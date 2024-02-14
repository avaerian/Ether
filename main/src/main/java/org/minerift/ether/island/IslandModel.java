package org.minerift.ether.island;

import org.jooq.Record;
import org.jooq.impl.SQLDataType;
import org.minerift.ether.database.sql.SQLDatabase;
import org.minerift.ether.database.sql.SQLResult;
import org.minerift.ether.database.sql.adapters.Adapters;
import org.minerift.ether.database.sql.model.Field;
import org.minerift.ether.database.sql.model.Model;
import org.minerift.ether.math.GridAlgorithm;

import java.util.UUID;

public class IslandModel extends Model<Island, Integer> {

    // TODO: remove after debugging model ctor and createField calls
    public static void main(String[] args) {
        new IslandModel(null);
    }

    // TODO: for upgrading databases, each table should contain a list of table upgraders

    // Database API allows for the definition of fields with complex data types, SQL data types, and safe fallback SQL data types.
    // - Complex data types are Java types that aren't SQL primitives. These types need to be adapted before using.
    // - SQL data types consist of all the types across supported dialects, whether they're universally supported or not.
    // - Safe SQL data types are fallback data types supported across all dialects. Useful for when the dialect selected
    //   doesn't support the desired SQL data type. In our case, dumping it to JSON and saving as text works fine.

    public final Field<Island, Integer, ?> ID           = createField("island_id", SQLDataType.INTEGER.notNull(), Island::getId);
    public final Field<Island, Boolean, ?> IS_DELETED   = createField("is_deleted", SQLDataType.BIT.notNull(), Island::isDeleted);
    public final Field<Island, UUID[],  ?> MEMBERS      = createField("members", SQLDataType.UUID.array(), Island::getTeamMembers, Adapters.ETHER_USERS_2_UUIDS);

    // TODO:        !! TRY THIS IMPLEMENTATION !!
    //  - For versioning, changes that are made on the developer-end need to be tracked.
    //  - ModelUpgradeSpec will provide a list of all the changes (table renames, col adds, dels, renames, etc.)
    //  - ModelUpgradeSpec will also provide functions to upgrade the data

    public IslandModel(SQLDatabase db) {
        super("islands", db);
        setupFields(ID, IS_DELETED, MEMBERS);
    }

    @Override
    public Island readResult(SQLResult<Island> result, Record record) {
        return Island.builder()
                .setTile(GridAlgorithm.computeTile(result.getField(ID, record)), true)
                .setDeleted(result.getField(IS_DELETED, record))
                .setMembers(Adapters.ETHER_USERS_2_UUIDS.adaptFrom(result.getField(MEMBERS, record))) // TODO: remove adapter
                .build();
    }

    @Override
    public Field<Island, Integer, ?> getPrimaryKey() {
        return ID;
    }
}
