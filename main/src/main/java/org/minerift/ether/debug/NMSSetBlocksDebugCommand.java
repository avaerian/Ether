package org.minerift.ether.debug;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.nms.NMS;
import org.minerift.ether.util.math.Vec3i;
import org.minerift.ether.world.QueuedBlock;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class NMSSetBlocksDebugCommand implements CommandExecutor {

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

        String mode = "SYNC"; // "SYNC", "ASYNC" -> default: "SYNC"
        if(args.length >= 1) {
            mode = args[0].toUpperCase();
        }

        if(args.length == 4) {
            width = Integer.parseInt(args[1]);
            height = Integer.parseInt(args[2]);
            length = Integer.parseInt(args[3]);
        }

        plr.sendMessage("Setting blocks...");
        NMS nmsAccess = EtherPlugin.getInstance().getNMS();
        switch (mode) {
            case "SYNC" -> nmsAccess.setBlocks(getTestCuboid(width, height, length), plr.getLocation());
            case "ASYNC" -> nmsAccess.setBlocksAsync(getTestCuboid(width, height, length), plr.getLocation());
        }
        plr.sendMessage("Blocks updated");

        return true;
    }

    private static Set<QueuedBlock> getTestCuboid(int width, int height, int length) {

        // Block data info
        final BlockData[] BLOCK_DATA = {
                Bukkit.createBlockData(Material.GLOWSTONE), // test for lighting issues
                Bukkit.createBlockData(Material.SPONGE),
                Bukkit.createBlockData(Material.AIR),
                Bukkit.createBlockData(Material.OAK_LOG)
        };
        final Random random = new Random();

        // Generate cuboid
        Set<QueuedBlock> blocks = new HashSet<>(width * height * length);
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int z = 0; z < length; z++) {
                    final BlockData randomBlockData = BLOCK_DATA[random.nextInt(BLOCK_DATA.length)];
                    blocks.add(new QueuedBlock(new Vec3i(x,y,z), randomBlockData));
                }
            }
        }

        return blocks;
    }
}
