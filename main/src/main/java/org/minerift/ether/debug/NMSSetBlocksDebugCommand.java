package org.minerift.ether.debug;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.NMSAccess;
import org.minerift.ether.util.BukkitUtils;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.world.BlockArchetype;

import java.util.*;

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

        String mode = "SYNC"; // "SYNC", "ASYNC", "DIST" -> default: "SYNC"
        if(args.length >= 1) {
            mode = args[0].toUpperCase();
        }

        if(args.length >= 4) {
            width = Integer.parseInt(args[1]);
            height = Integer.parseInt(args[2]);
            length = Integer.parseInt(args[3]);
        }

        plr.sendMessage("Setting blocks...");
        //NMSAccess nmsAccess = EtherPlugin.getInstance().getNMS();
        final NMSAccess nmsAccess = Ether.getNMS();

        // Get cuboid and translate to player pos
        List<BlockArchetype> cuboid = getTestCuboid(width, height, length);
        cuboid.forEach(block -> block.getPos().add(BukkitUtils.asVec3i(plr.getLocation())));

        // Set blocks based on mode
        switch (mode) {
            case "SYNC" -> nmsAccess.setBlocks(cuboid, plr.getWorld());
            case "ASYNC" -> nmsAccess.setBlocksAsync(cuboid, plr.getWorld());
            case "DIST" -> nmsAccess.setBlocksAsyncLazy(cuboid, plr.getWorld());
        }
        plr.sendMessage("Blocks updated");

        return true;
    }

    private static List<BlockArchetype> getTestCuboid(int width, int height, int length) {

        // Block data info
        final BlockData[] BLOCK_DATA = {
                Bukkit.createBlockData(Material.GLOWSTONE), // test for lighting issues
                Bukkit.createBlockData(Material.SPONGE),
                Bukkit.createBlockData(Material.AIR),
                Bukkit.createBlockData(Material.OAK_LOG)
        };

        /*
        // Block data info
        final BlockData[] BLOCK_DATA = {
                Bukkit.createBlockData(Material.CHEST),
                Bukkit.createBlockData(Material.STONE),
                Bukkit.createBlockData(Material.SEA_LANTERN),
                Bukkit.createBlockData(Material.LIME_STAINED_GLASS),
                Bukkit.createBlockData(Material.FURNACE),
                Bukkit.createBlockData(Material.AIR)
        };*/
        final Random random = new Random();

        // Generate cuboid
        List<BlockArchetype> blocks = new ArrayList<>(width * height * length);
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                for(int z = 0; z < length; z++) {
                    final BlockData randomBlockData = BLOCK_DATA[random.nextInt(BLOCK_DATA.length)];
                    blocks.add(new BlockArchetype(randomBlockData.getAsString(true), new Vec3i(x,y,z)));
                }
            }
        }

        return blocks;
    }
}
