package org.minerift.ether.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFormEvent;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.blocks.BlocksConfig;
import org.minerift.ether.dimension.Dimension;
import org.minerift.ether.nms.world.block.BlockState;
import org.minerift.ether.util.BukkitUtils;
import org.minerift.ether.util.UnreachableException;

public class OnBlockFormListener implements Listener {

    @EventHandler
    public void onBlockForm(BlockFormEvent e) {
        // cobblestone generator handling
        BlocksConfig cfg = Ether.inst().getConfig(ConfigType.BLOCKS);
        BlockState<?> newState = BlockState.of(e.getNewState());
        if(newState.is("minecraft:stone") && cfg.useCobblestoneGen()) {
            e.setCancelled(true);
            BlockState ore = switch (cfg.getCobblestoneGenHandling()) {
                case PER_DIMENSION -> {
                    Dimension dim = Dimension.from(e.getBlock().getWorld());
                    if(dim == null)
                        yield null;
                    yield null; // TODO
                }
                case GLOBAL -> cfg.getMainCobblestoneGen().selectRandom();
                case NONE -> throw new UnreachableException("unexpected");
            };
            if(ore == null)
                return;
            Bukkit.broadcast(Component.text("Ore: " + ore));
            Ether.inst().getNms().setBlockState(e.getBlock().getWorld(), BukkitUtils.asVec3i(e.getBlock().getLocation()), ore);
            Ether.inst().getLogger().info("Ore block formed from cobble generator");
        }
    }

}
