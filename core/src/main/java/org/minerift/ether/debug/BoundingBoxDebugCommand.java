package org.minerift.ether.debug;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.nms.world.ChunkGetter;
import org.minerift.ether.schematic.data.*;
import org.minerift.ether.schematic.sponge.Pasters;
import org.minerift.ether.work.Work;

import java.util.concurrent.Executors;

import static org.minerift.ether.util.BukkitUtils.asVec3i;

@Debug
public class BoundingBoxDebugCommand implements CommandExecutor {

    // cmd: /bb <pos1|pos2|create|paste|pop>

    /* TEMP */
    private Object2ObjectMap<String, BoundingBox> bbs;
    private SelectSessions sessions;
    public BoundingBoxDebugCommand(SelectSessions sessions) {
        this.sessions = sessions;
        this.bbs = new Object2ObjectOpenHashMap<>();
    }

    public BoundingBoxDebugCommand() {
        this(new SelectSessions());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player plr)) {
            sender.sendMessage("Must execute as a player");
            return false;
        }

        switch (args[0]) {
            case "pos1" -> {
                SelectSessions.Selection s = sessions.selectPos1(plr.getUniqueId(), asVec3i(plr.getLocation()));
                plr.sendMessage("pos1 set " + s.pos1);
            }
            case "pos2" -> {
                SelectSessions.Selection s = sessions.selectPos2(plr.getUniqueId(), asVec3i(plr.getLocation()));
                plr.sendMessage("pos2 set " + s.pos2);
            }
            case "create" -> {
                SelectSessions.Selection s = sessions.selectFor(plr.getUniqueId());
                BoundingBox bb = bbs.compute(args[1], (_s, _bb) -> s.box());
                plr.sendMessage("BoundingBox created (pos1=" + s.pos1 + ", pos2=" + s.pos2 + ", min=" + bb.min + ", max=" + bb.max + ")");
            }
            case "paste" -> {
                BoundingBox box = bbs.get(args[1]);
                final ChunkGetter cg;
                if(args.length >= 3) {
                    cg = ChunkGetter.valueOf(args[2].toUpperCase());
                } else {
                    cg = ChunkGetter.SYNC;
                }

                box.getBlocksMut(plr.getWorld(),
                        Array3DOrder.YZX, cg)
                        .thenApply(BlockVolume.Builder::build)
                        .thenAccept((bv) -> {
                            Work<Void> pasteTask = Pasters.pasteBlockVolume(bv, plr.getWorld(), asVec3i(plr.getLocation()), cg)
                                    .whenComplete((w) -> plr.sendMessage("Pasted successfully"));
                            Ether.inst().getWorkQueue().enqueue(pasteTask);
                        });
            }
            case "pop" -> {
                sessions.pop(plr.getUniqueId());
                plr.sendMessage("popped session");
            }
        }

        return true;
    }
}
