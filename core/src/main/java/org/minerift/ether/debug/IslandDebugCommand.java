package org.minerift.ether.debug;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.config.islandspecs.IslandSpec;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.schematic.Schematic;
import org.minerift.ether.schematic.SchematicReadException;
import org.minerift.ether.schematic.SchematicType;
import org.minerift.ether.user.EtherUser;

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
                .orElseThrow(() -> new UnsupportedOperationException(String.format("User %s not found", plr.getUniqueId())));

        Schematic schem;
        try {
            schem = Schematic.fromFile(SchematicType.SPONGE, Ether.inst().getPluginFile("test_schem1.schem"));
        } catch (SchematicReadException e) {
            throw new RuntimeException(e);
        }

        IslandSpec spec = IslandSpec.builder()
                .setSchematic(schem)
                .build();

        switch(args[0].toLowerCase()) {
            // island create
            case "create"   -> Ether.inst().getIslandManager().createIsland(plr.getWorld(), spec, user);
            // island delete <x> <z>
            case "delete"   -> Ether.inst().getIslandManager().deleteIsland(new Vec2i(Integer.parseInt(args[1]), Integer.parseInt(args[2])), ChunkGetter.ASYNC);
            // island get
            case "get"      -> {
                var optIsland = Ether.inst().getIslandManager().getIslandAt(plr.getLocation());
                optIsland.ifPresentOrElse(
                        (island) -> plr.sendMessage("You are standing in island " + island.getTile() + " : " + island.getId()),
                        ()       -> plr.sendMessage("You aren't standing in any island!")
                );
            }
            default -> {
                plr.sendMessage(String.format("Unrecognized argument \"%s\"", args[0]));
                return false;
            }
        }

        return true;
    }
}
