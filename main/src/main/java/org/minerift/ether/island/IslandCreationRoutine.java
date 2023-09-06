package org.minerift.ether.island;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.types.Schematic;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.math.Vec3i;

import java.io.File;

public class IslandCreationRoutine {

    public static Island run(IslandGrid grid, EtherUser user) {

        Preconditions.checkArgument(user.getOfflinePlayer().isOnline(), "User must be online to create island!");
        final Player plr = (Player) user.getOfflinePlayer();

        final Tile tile = grid.getNextTile();
        final Island island = Island.builder()
                .setTile(tile, true)
                .setDeleted(false)
                .addTeamMember(user, IslandRole.OWNER)
                .build();

        grid.registerIsland(island);

        // TODO: Paste schematic/structure onto tile
        File file = null;
        Vec3i pos = Vec3i.ZERO;
        Schematic.fromFile(file).handle((schem) -> {
            schem.paste(pos, plr.getWorld().getName(), SchematicPasteOptions.builder().copyBiomes(true).build());
        }, (ex) -> { throw new RuntimeException(ex); });

        // Teleport player

        return island;

    }

}
