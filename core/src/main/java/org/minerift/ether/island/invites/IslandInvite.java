package org.minerift.ether.island.invites;

import com.google.common.base.Objects;
import org.minerift.ether.Ether;
import org.minerift.ether.config.ConfigType;
import org.minerift.ether.config.main.MainConfig;
import org.minerift.ether.island.Island;
import org.minerift.ether.util.pair.UUIDPair;

import java.security.SecureRandom;
import java.util.UUID;

public final class IslandInvite {

    public static final int UNEXPIRABLE = -1;
    public static final int MANUALLY_EXPIRED = -2;

    public static final SecureRandom INVITE_ID_GENERATOR = new SecureRandom();

    private final int uniqueId;
    private final UUID sender;
    private final UUID receiver;
    private final Island island;
    private long expire;


    // TODO: refactor from int id to UUIDv7 (something time based)

    public static IslandInvite create(UUID sender, UUID receiver, Island island, boolean expires) {
        long expiration;
        if(expires) {
            final MainConfig config = Ether.getConfig(ConfigType.MAIN);
            expiration = System.currentTimeMillis() + config.getInviteInvalidateAfter();
        } else {
            expiration = UNEXPIRABLE;
        }
        return create(sender, receiver, island, expiration);
    }

    public static IslandInvite create(UUID sender, UUID receiver, Island island, long expireTimestamp) {
        return new IslandInvite(INVITE_ID_GENERATOR.nextInt(), sender, receiver, island, expireTimestamp);
    }

    public static IslandInvite create(UUIDPair senderReceiver, Island island, boolean expires) {
        return create(senderReceiver.getFirst(), senderReceiver.getSecond(), island, expires);
    }

    public static IslandInvite create(UUIDPair senderReceiver, Island island, long expireTimestamp) {
        return create(senderReceiver.getFirst(), senderReceiver.getSecond(), island, expireTimestamp);
    }

    public IslandInvite(int uniqueId, UUID sender, UUID receiver, Island island, long expireTimestamp) {
        this.uniqueId = uniqueId;
        this.sender = sender;
        this.receiver = receiver;
        this.island = island;
        this.expire = expireTimestamp;
    }

    public int getInviteId() {
        return uniqueId;
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
