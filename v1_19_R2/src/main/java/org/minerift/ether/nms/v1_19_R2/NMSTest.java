package org.minerift.ether.nms.v1_19_R2;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld;

public class NMSTest {

    private void test() {

        // A random method including NMS method calls
        // Test for compilation
        World world = Bukkit.getWorld("test-world");
        ServerLevel level = ((CraftWorld) world).getHandle();
        LevelChunkSection sections[] = level.getChunk(0, 0).getSections();



    }

}
