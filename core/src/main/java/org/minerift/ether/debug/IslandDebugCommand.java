package org.minerift.ether.debug;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.config.islandspecs.IslandSpec;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.SchematicType;
import org.minerift.ether.user.EtherUser;

import static java.lang.String.format;
import static org.minerift.ether.util.BukkitUtils.asVec3i;

public class IslandDebugCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player plr)) {
            sender.sendMessage("Must be player to execute command!");
            return false;
        }

        if(args.length == 0) {
            plr.sendMessage("No arguments found.");
            return false;
        }

        EtherUser user = Ether.inst().getUserManager().getUser(plr.getUniqueId())
                .orElseThrow(() -> new UnsupportedOperationException(format("User %s not found", plr.getUniqueId())));

        Schematic schem;
        try {
            schem = Schematic.fromFile(SchematicType.SPONGE, Ether.inst().getPluginFile("test_schem1.schem"));
        } catch (SchematicReadException e) {
            plr.sendMessage(Component.text("Failed to read schematic test_schem1.schem"));
            return false;
        }

        IslandSpec spec = IslandSpec.builder()
                .setSchematic(schem)
                .setIslandName("Test Island")
                //.setSpawns(SingleSpawn.of(Vec3d.ZERO))
                .build();

        switch(args[0].toLowerCase()) {
            // island create
            case "create"   -> {
                Dimension dim = Dimension.from(plr.getWorld());
                Ether.inst().getIslandManager().createIsland(dim, spec, user);
            }
            // island delete <x> <z>
            case "delete"   -> Ether.inst().getIslandManager().deleteIsland(new Vec2i(Integer.parseInt(args[1]), Integer.parseInt(args[2])), ChunkGetter.ASYNC);
            // island get
            case "get"      -> {
                var optIsland = Ether.inst().getIslandManager().getIslandAt(asVec3i(plr.getLocation()));
                optIsland.ifPresentOrElse(
                        (island) -> plr.sendMessage("You are standing in island " + island.getTile() + " : " + island.getId()),
                        ()       -> plr.sendMessage("You aren't standing in any island!")
                );
            }
            default -> {
                plr.sendMessage(format("Unrecognized argument \"%s\"", args[0]));
                return false;
            }
        }

        return true;
    }
}
