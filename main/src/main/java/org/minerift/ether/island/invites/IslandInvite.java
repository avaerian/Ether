package org.minerift.ether.island.invites;

import com.google.common.base.Objects;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.island.Island;
import org.minerift.ether.util.pair.UUIDPair;

import java.util.UUID;

public final class IslandInvite {

    public static final int UNEXPIRABLE = -1;
    public static final int MANUALLY_EXPIRED = -2;

    private final UUID sender;
    private final UUID receiver;
    private final Island island;
    private long expire;

    public IslandInvite(UUID sender, UUID receiver, Island island, boolean expires) {
        this.sender = sender;
        this.receiver = receiver;
        this.island = island;

        if(expires) {
            final MainConfig config = Ether.getConfig(ConfigType.MAIN);
            this.expire = System.currentTimeMillis() + config.getInviteInvalidateAfter();
        } else {
            this.expire = UNEXPIRABLE;
        }
    }

    public IslandInvite(UUID sender, UUID receiver, Island island, long expireTimestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.island = island;
        this.expire = expireTimestamp;
    }

    public IslandInvite(UUIDPair senderReceiver, Island island, boolean expires) {
        this(senderReceiver.getFirst(), senderReceiver.getSecond(), island, expires);
    }

    public IslandInvite(UUIDPair senderReceiver, Island island, long expireTimestamp) {
        this(senderReceiver.getFirst(), senderReceiver.getSecond(), island, expireTimestamp);
    }

    public UUID getSender() {
        return sender;
    }

    public UUID getReceiver() {
        return receiver;
    }

    public UUIDPair getSenderReceiver() {
        return new UUIDPair(sender, receiver);
    }

    public Island getIsland() {
        return island;
    }

    public void setExpired() {
        this.expire = MANUALLY_EXPIRED;
    }

    public boolean isExpiredManually() {
        return expire == MANUALLY_EXPIRED;
    }

    public boolean isExpirable() {
        return expire != UNEXPIRABLE;
    }

    public long getExpireTimestamp() {
        return expire;
    }

    public boolean isExpired() {
        return isExpiredManually() || (isExpirable() && System.currentTimeMillis() > expire);
    }

    public boolean accept() {
        return Ether.getInviteManager().accept(this);
    }

    public boolean deny() {
        return Ether.getInviteManager().deny(this);
    }

    @Override
    public String toString() {
        return "IslandInvite{" +
                "sender=" + sender +
                ", receiver=" + receiver +
                ", island=" + island +
                ", expire=" + expire +
                '}';
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
