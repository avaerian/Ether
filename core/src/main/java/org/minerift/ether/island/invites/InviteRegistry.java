package org.minerift.ether.island.invites;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import org.jetbrains.annotations.NotNull;
import org.minerift.ether.Ether;
import org.minerift.ether.util.pair.UUIDPair;

import java.util.*;

public class InviteRegistry implements Iterable<IslandInvite> {

    // TODO: timed cache would be nice

    // Row is sender
    // Column is receiver
    private final Table<UUID, UUID, IslandInvite> inviteTable;

    public InviteRegistry() {
        this.inviteTable = HashBasedTable.create();
    }

    // Returns whether invite was registered successfully
    public boolean register(IslandInvite invite) {
        if(inviteTable.contains(invite.getSender(), invite.getReceiver())) {
            Ether.getLogger().warning("Cannot register island invite because existing invite exists between players: \n"
                                         + "{Sender:" + invite.getSender() + "} {Receiver:" + invite.getReceiver() + "}");
            return false;
        }

        inviteTable.put(invite.getSender(), invite.getReceiver(), invite);
        return true;
    }

    public int size() {
        return inviteTable.size();
    }

    public Set<UUIDPair> getKeySet() {
        var cellSet = inviteTable.cellSet();
        Set<UUIDPair> keys = new HashSet<>(cellSet.size());
        for(Table.Cell<UUID, UUID, IslandInvite> cell : cellSet) {
            keys.add(new UUIDPair(cell.getRowKey(), cell.getColumnKey()));
        }
        return keys;
    }

    public IslandInvite get(UUID sender, UUID receiver) {
        return inviteTable.get(sender, receiver);
    }

    public boolean contains(UUID sender, UUID receiver) {
        return inviteTable.contains(sender, receiver);
    }

    public boolean contains(IslandInvite invite) {
        return contains(invite.getSender(), invite.getReceiver());
    }

    // Returns old invite, if any
    public IslandInvite remove(UUID sender, UUID receiver) {
        return inviteTable.remove(sender, receiver);
    }

    public IslandInvite remove(IslandInvite invite) {
        return remove(invite.getSender(), invite.getReceiver());
    }

    // Outgoing invites = user is sender
    public List<IslandInvite> getOutgoingInvites(UUID sender) {
        return List.copyOf(inviteTable.row(sender).values());
    }

    // Incoming invites = user is receiver
    public List<IslandInvite> getIncomingInvites(UUID receiver) {
        return List.copyOf(inviteTable.column(receiver).values());
    }

    public List<IslandInvite> getAllInvites() {
        return List.copyOf(inviteTable.values());
    }

    protected Table<UUID, UUID, IslandInvite> getUnderlyingTable() {
        return inviteTable;
    }

    // Returns number of purged invites
    public int purgeExpiredInvites() {
        int originalSize = inviteTable.size();
        inviteTable.values().removeIf(IslandInvite::isExpired);
        System.out.println(inviteTable.values());
        return originalSize - inviteTable.size();
    }

    @NotNull
    @Override
    public Iterator<IslandInvite> iterator() {
        return inviteTable.values().iterator();
    }
}
