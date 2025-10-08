package org.minerift.ether.island;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.SchematicPasteOptions;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.user.EtherUser;

import java.io.File;

@Deprecated
public class IslandCreationRoutine {

    public static Island run(IslandGrid grid, EtherUser user) {

        final Player plr = user.getPlayer().orElseThrow(() -> new IllegalArgumentException("User must be online to create island"));
        final World islandWorld = plr.getWorld(); // TODO: change this to island world (add config thing and load in Ether class)

        final MainConfig config = Ether.getConfig(ConfigType.MAIN);
        //final SchematicsConfig schemConfig = Ether.getConfig(ConfigType.SCHEM_LIST);

        // Grid tile starts bottom left
        final Vec2i tile = grid.getNextTile();
        System.out.println("Tile = " + tile);
        final Vec2i bottomLeftChunk = new Vec2i(tile.getX() * config.getTileLengthChunks(), tile.getZ() * config.getTileLengthChunks());
        final Vec2i topRightChunk = new Vec2i((config.getTileLengthChunks() * (tile.getX() + 1)) - 1, (config.getTileLengthChunks() * (tile.getZ() + 1)) - 1);

        System.out.println("bottomLeftChunk = " + bottomLeftChunk);
        System.out.println("topRightChunk = " + topRightChunk);

        final Island island = Island.builder()
                .setTile(tile, true)
                .setBottomLeftBound(bottomLeftChunk)
                .setTopRightBound(topRightChunk)
                .setDeleted(false)
                .setOwner(user)
                .build();

        System.out.println("bottomLeftBlock = " + island.getBottomLeftBlock());
        System.out.println("topRightBlock = " + island.getTopRightBlock());

        // Paste island at tile
        // Refer to the Island Placement Graph (https://www.desmos.com/calculator/fuwvk1rgkf) for easy maths and representation
        //final File schemFile = new File(Ether.getPluginDir(), "test_schem1.schem"); // TODO: move this into function parameter
        File schemFile = Ether.getPluginFile("test_schem1.schem");

        // Get schematic paste position
        // OLD CODE:
        //final int bottomLeftOffset = (config.getTileSize() / 2) - (config.getTileAccessibleArea() / 2);
        //bottomLeftPos.add(bottomLeftOffset, 0, bottomLeftOffset);

        // TODO: this needs to be the center of the island, with the schematic offset being the middle of the schematic
        final int halfTile = config.getTileLengthBlocks() / 2;
        System.out.println("halfTile = " + halfTile);
        Vec3i.Mutable tileCenterPos = island.getBottomLeftBlock().asMutable().add(halfTile, 0, halfTile); //.transform(x -> x + halfTile, y -> config.getTileHeight(), z -> z + halfTile);
        tileCenterPos.setY(config.getTileHeight());
        System.out.println("Tile Center Pos = " + tileCenterPos);

        try {
            final Schematic schem = Schematic.fromFile(schemFile);
            final Vec3i schemCenterOffset = new Vec3i.Mutable(schem.getWidth() / 2, schem.getHeight() / 2, schem.getLength() / 2); //.transform((x) -> -x, (y) -> -y, (z) -> -z);
            //final Vec3i schemCenterOffset = new Vec3i.Mutable(-schem.getWidth() / 2, -schem.getHeight() / 2, -schem.getLength() / 2); // try subtracted offset?
            System.out.println("Schem center offset = " + schemCenterOffset);
            final SchematicPasteOptions options = SchematicPasteOptions.builder()
                    .setOffset(schemCenterOffset) // set pivot around center block
                    .copyBiomes(true)
                    .copyEntities(false)
                    .ignoreAirBlocks(false)
                    .build();

            schem.paste(tileCenterPos, islandWorld.getName(), options);
        } catch (SchematicReadException ex) {
            throw new RuntimeException(ex);
        }

        // Register island after paste operation is successful
        grid.registerIsland(island);

        // Teleport player
        // TODO: find sign and spawn player in front

        Location plrTp = new Location(islandWorld, tileCenterPos.getX(), tileCenterPos.getY() + 2, tileCenterPos.getZ());
        plr.teleportAsync(plrTp, PlayerTeleportEvent.TeleportCause.PLUGIN);

        return island;

    }

}
