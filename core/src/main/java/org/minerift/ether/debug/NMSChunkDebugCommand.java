package org.minerift.ether.debug;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.math.Vec2i;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.nms.world.ChunkGetter;

import java.util.concurrent.CompletableFuture;

public class NMSChunkDebugCommand implements CommandExecutor {


    // /nmschunk <diameter> <mode>

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Please use this command as a player");
            return false;
        }

        int diameter = 1;
        if(args.length >= 1) {
            diameter = Integer.parseInt(args[0]);
        }

        String mode = "CLEAR"; // modes: "CLEAR", "REGEN", "ASYNC"
        if(args.length >= 2) {
            mode = args[1].toUpperCase();
        }

        Player plr = (Player) sender;
        World world = plr.getWorld();
        final NMSAccess nms = Ether.inst().getNms();

        int centerX = plr.getChunk().getX();
        int centerZ = plr.getChunk().getZ();

        int radius = (diameter - 1) / 2;

        Vec2i e1 = new Vec2i(centerX - radius, centerZ - radius);
        Vec2i e2 = new Vec2i(centerX + radius, centerZ + radius);

        // Perform action
        final ChunkGetter cg;
        switch(mode) {
            case "ASYNC" -> cg = ChunkGetter.ASYNC;
            default -> cg = ChunkGetter.SYNC;
        }

        CompletableFuture<Void>[] futures =
                new CompletableFuture[(e2.getX() - e1.getX()) * (e2.getZ() - e1.getZ())];
        for(int z = e1.getZ(), i = 0; z < e2.getZ(); z++) {
            for(int x = e1.getX(); x < e2.getX(); x++) {
                futures[i++] = cg.getChunk(world, x, z)
                        .thenAcceptAsync((chunk) -> nms.clearChunk(chunk, true));
            }
        }
        CompletableFuture.allOf(futures).thenRun(() -> plr.sendMessage("Chunks cleared"));

        return true;
    }

}