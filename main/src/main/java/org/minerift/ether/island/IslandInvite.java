package org.minerift.ether.island;

import com.google.common.base.Objects;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.user.EtherUser;

import java.util.UUID;

public class IslandInvite {

    private final UUID sender;
    private final EtherUser receiver;
    private final Island island;
    private final long expire;

    public IslandInvite(UUID sender, EtherUser receiver, Island island, boolean expires) {
        this.sender = sender;
        this.receiver = receiver;
        this.island = island;

        if(expires) {
            final MainConfig config = Ether.getConfig(ConfigType.MAIN);
            this.expire = System.currentTimeMillis() + config.getInviteInvalidateAfter();
        } else {
            this.expire = -1;
        }

        Ether.getInviteManager().register(this);
    }

    public UUID getSender() {
        return sender;
    }

    public EtherUser getReceiver() {
        return receiver;
    }

    public Island getIsland() {
        return island;
    }

    public long getExpireTimestamp() {
        return expire;
    }

    public boolean isExpired() {
        return Ether.getInviteManager().isExpired(this);
    }

    public boolean accept() {
        return Ether.getInviteManager().accept(this);
    }

    public boolean deny() {
        return Ether.getInviteManager().deny(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IslandInvite that = (IslandInvite) o;
        return Objects.equal(sender, that.sender) && Objects.equal(receiver, that.receiver) && Objects.equal(island, that.island);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sender, receiver, island);
    }
}
