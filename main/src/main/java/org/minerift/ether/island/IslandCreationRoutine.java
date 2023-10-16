package org.minerift.ether.island;

import org.bukkit.entity.Player;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.SchematicFileReadException;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.types.Schematic;
import org.minerift.ether.user.EtherUser;

import java.io.File;

public class IslandCreationRoutine {

    public static Island run(IslandGrid grid, EtherUser user) {

        final Player plr = user.getPlayer().orElseThrow(() -> new IllegalArgumentException("User must be online to create island!"));

        final MainConfig config = Ether.getConfig(ConfigType.MAIN);
        //final SchematicsConfig schemConfig = Ether.getConfig(ConfigType.SCHEM_LIST);

        // Register island on grid
        final Vec2i tile = grid.getNextTile();
        final Island island = Island.builder()
                .setTile(tile, true)
                .setDeleted(false)
                .setOwner(user)
                .build();

        grid.registerIsland(island);

        // Paste island at tile
        // Refer to the Island Placement Graph (https://www.desmos.com/calculator/fuwvk1rgkf) for easy maths and representation
        final File file = null; // TODO

        // Get schematic paste position

        // OLD CODE:
        //final int bottomLeftOffset = (config.getTileSize() / 2) - (config.getTileAccessibleArea() / 2);
        //bottomLeftPos.add(bottomLeftOffset, 0, bottomLeftOffset);

        // TODO: this needs to be the center of the island, with the schematic offset being the middle of the schematic
        final int halfTile = config.getTileSize() / 2;
        Vec3i.Mutable tileCenterPos = island.getBottomLeftBlock().asMutable().add(halfTile, 0, halfTile);
        tileCenterPos.setY(config.getTileHeight());

        try {
            final Schematic schem = Schematic.fromFile(file);
            final Vec3i schemCenterOffset = new Vec3i(schem.getWidth() / 2, schem.getHeight() / 2, schem.getLength() / 2);
            final SchematicPasteOptions options = SchematicPasteOptions.builder()
                    .setOffset(schemCenterOffset)
                    .copyBiomes(true)
                    .copyEntities(false)
                    .ignoreAirBlocks(false)
                    .build();

            schem.paste(tileCenterPos, plr.getWorld().getName(), options);
        } catch (SchematicFileReadException ex) {
            throw new RuntimeException(ex);
        }

        // Teleport player
        // TODO: find sign and spawn player in front

        return island;

    }

}
