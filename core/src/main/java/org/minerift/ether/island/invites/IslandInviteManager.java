package org.minerift.ether.island.invites;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.minerift.ether.Ether;
import org.minerift.ether.EtherPlugin;
import org.minerift.ether.island.IslandRole;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.util.pair.UUIDPair;

import java.util.*;
import java.util.function.Function;

import static org.minerift.ether.math.Maths.TICKS_PER_SEC;

public class IslandInviteManager {

    public static IslandInviteManager noPurgeTask(InviteRegistry registry) {
        return new IslandInviteManager(registry, null);
    }

    public static IslandInviteManager noPurgeTask() {
        return noPurgeTask(new InviteRegistry());
    }

    protected final InviteRegistry invites;
    private final BukkitTask inviteTask;

    // Use a pre-existing registry (let's say, for example, loaded from a persistence system)
    public IslandInviteManager(InviteRegistry registry) {
        this.invites = registry;
        this.inviteTask = Bukkit.getScheduler().runTaskTimer(
                EtherPlugin.getInstance(), invites::purgeExpiredInvites, 0, 10 * TICKS_PER_SEC); // run every 10 secs
    }

    private IslandInviteManager(InviteRegistry registry, BukkitTask task) {
        this.invites = registry;
        this.inviteTask = task;
    }

    public IslandInviteManager() {
        this(new InviteRegistry());
    }

    public void register(IslandInvite invite) {
        invites.register(invite);
    }

    public Set<UUIDPair> getKeySet() {
        return invites.getKeySet();
    }

    public List<IslandInvite> getOutgoingInvites(UUID sender) {
        return invites.getOutgoingInvites(sender);
    }

    public List<IslandInvite> getIncomingInvites(UUID receiver) {
        return invites.getIncomingInvites(receiver);
    }

    // Handles an island invite with a callback and consumes the invite after done
    private boolean handleAndConsume(IslandInvite invite, Function<EtherUser, Boolean> callback) {
        EtherUser receiver = Ether.inst().getUserManager().getUser(invite.getReceiver())
                .orElseThrow(() -> new UnsupportedOperationException("User must be online to accept an island invite!"));
        Player plr = receiver.getPlayer().orElseThrow();

        try {
            if(invite.isExpired()) {
                plr.sendMessage("Sorry! The invite has already expired.");
                return false;
            }

            return callback.apply(receiver);
        } finally {
            invites.remove(invite); // always consume invite
        }
    }

    // Returns whether the invite was able to be accepted successfully
    // Receiver accepts requested island invite
    protected boolean accept(IslandInvite invite) {
        return handleAndConsume(invite, (receiver) -> {
            invite.getIsland().addTeamMember(receiver, IslandRole.MEMBER);
            return true;
        });
    }

    protected boolean deny(IslandInvite invite) {
        return handleAndConsume(invite, (receiver) -> true);
    }

    public List<IslandInvite> getAllInvites() {
        return invites.getAllInvites();
    }

    public BukkitTask getTask() {
        return inviteTask;
    }

}
