package org.minerift.ether.debug;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.schematic.data.BlockVolume;
import org.minerift.ether.schematic.data.BytePalette;
import org.minerift.ether.schematic.sponge.Pasters;
import org.minerift.ether.util.BukkitUtils;
import org.minerift.ether.work.Work;

import java.util.*;

import static org.minerift.ether.schematic.data.Array3DOrder.YZX;

public class NmsSetBlocksDebugCommand implements CommandExecutor {

    // /nmsblock <mode> <width> <height> <length>

    @NeedsTesting
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof ConsoleCommandSender) {
            sender.sendMessage("Please use this command as a player");
            return false;
        }
        Player plr = (Player) sender;

        int width = 3;
        int height = 4;
        int length = 5;

        String mode = "SYNC"; // "SYNC", "ASYNC"
        if(args.length >= 1) {
            mode = args[0].toUpperCase();
        }

        if(args.length >= 4) {
            width = Integer.parseInt(args[1]);
            height = Integer.parseInt(args[2]);
            length = Integer.parseInt(args[3]);
        }

        plr.sendMessage("Setting blocks...");

        BytePalette<BlockState<?>> palette = BytePalette.of(4);
        palette.add((byte) 0, BlockState.of("glowstone", Material.GLOWSTONE.getKey().asString()));
        palette.add((byte) 1, BlockState.of("sponge", Material.SPONGE.getKey().asString()));
        palette.add((byte) 2, BlockState.of("air", Material.AIR.getKey().asString()));
        palette.add((byte) 3, BlockState.of("oak_log", Material.OAK_LOG.getKey().asString()));

        BlockVolume bv = BlockVolume.builder()
                .setOrder(YZX)
                .setDimensions(width, height, length)
                .setPalette(palette)
                .setData(genJunkData(width, height, length, palette))
                .build();

        ChunkGetter cg = switch (mode) {
            case "SYNC"     -> ChunkGetter.SYNC;
            case "ASYNC"    -> ChunkGetter.ASYNC;
            default -> throw new IllegalStateException("Unexpected value: " + mode);
        };
        Work<Void> pasteTask = Pasters.pasteBlockVolume(bv, plr.getWorld(), BukkitUtils.asVec3i(plr.getLocation()), cg)
                .whenComplete((w) -> plr.sendMessage("Blocks updated"));

        Ether.inst().getWorkQueue().enqueue(pasteTask);
        return true;
    }

    private static byte[] genJunkData(int width, int height, int length, BytePalette<BlockState<?>> palette) {
        final Random random = new Random();
        byte[] data = new byte[width * height * length];
        for(int i = 0; i < data.length; i++) {
            data[i] = (byte) random.nextInt(palette.size());
        }
        return data;
    }
}
