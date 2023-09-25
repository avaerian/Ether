package org.minerift.ether.island;

import org.bukkit.entity.Player;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.config.schems.SchematicConfig;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.types.Schematic;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.BukkitUtils;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;

import java.io.File;

public class IslandCreationRoutine {

    public static Island run(IslandGrid grid, EtherUser user) {

        final Player plr = user.getPlayer().orElseThrow(() -> new IllegalArgumentException("User must be online to create island!"));

        final MainConfig config = Ether.getConfig(ConfigType.MAIN);
        final SchematicConfig schemConfig = Ether.getConfig(ConfigType.SCHEM_LIST);

        // Register island on grid
        final Vec2i tile = grid.getNextTile();
        final Island island = Island.builder()
                .setTile(tile, true)
                .setDeleted(false)
                .addTeamMember(user, IslandRole.OWNER)
                .build();

        grid.registerIsland(island);

        // Paste island at tile
        // Refer to the Island Placement Graph (https://www.desmos.com/calculator/fuwvk1rgkf) for easy maths and representation
        final File file = null; // TODO

        // Get schematic paste position
        Vec3i.Mutable bottomLeftPos = BukkitUtils.getVec3iAt(tile).asMutable(); // TODO
        final int bottomLeftOffset = (config.getTileSize() / 2) - (config.getTileAccessibleArea() / 2);
        bottomLeftPos.add(bottomLeftOffset, 0, bottomLeftOffset);

        try {
            final Schematic schem = Schematic.fromFile(file);
            final SchematicPasteOptions options = SchematicPasteOptions.builder()
                    .setOffset(bottomLeftPos)
                    .copyBiomes(true)
                    .copyEntities(false)
                    .ignoreAirBlocks(false)
                    .build();

            schem.paste(bottomLeftPos, plr.getWorld().getName(), options);
        } catch (SchematicFileReadException ex) {
            throw new RuntimeException(ex);
        }

        // Teleport player
        // TODO: find sign and spawn player in front

        return island;

    }

}
