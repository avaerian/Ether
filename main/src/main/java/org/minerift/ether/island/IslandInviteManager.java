package org.minerift.ether.island;

import java.util.*;

public class IslandInviteManager {

    // TODO: create a task that purges expired invites

    private final Set<IslandInvite> invites;

    public IslandInviteManager() {
        this.invites = new HashSet<>();
    }

    protected void register(IslandInvite invite) {
        invites.add(invite);
    }

    public List<IslandInvite> getOutgoingInvites(UUID sender) {
        List<IslandInvite> invites = new ArrayList<>();
        for(IslandInvite invite : invites) {
            if(invite.getSender().equals(sender)) {
                invites.add(invite);
            }
        }
        return invites;
    }

    // Returns whether the invite was able to be accepted successfully
    protected boolean accept(IslandInvite invite) {
        if(invite.isExpired()) {
            // TODO: log to player
            invites.remove(invite);
            return false;
        }

        invite.getIsland().addTeamMember(invite.getReceiver(), IslandRole.MEMBER);
        invites.remove(invite);
        return true;
    }

    protected boolean deny(IslandInvite invite) {
        if(invite.isExpired()) {
            // TODO: log to player
            invites.remove(invite);
            return false;
        }

        // TODO: log to player
        invites.remove(invite);
        return true;
    }

    protected boolean isExpired(IslandInvite invite) {
        return System.currentTimeMillis() > invite.getExpireTimestamp() || !invites.contains(invite);
    }

    public Set<IslandInvite> getAllInvites() {
        return Set.copyOf(invites);
    }

}
