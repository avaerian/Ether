package org.minerift.ether.user;

import java.util.*;

// TODO: needs work
public class UserManager {

    private Map<UUID, EtherUser> users;

    public UserManager() {
        this.users = new HashMap<>();
    }

    // Returns whether the user was registered successfully)
    public boolean register(EtherUser user) {
        return users.putIfAbsent(user.getUUID(), user) == null;
    }

    public Optional<EtherUser> getUser(UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    public Set<UUID> getKeySet() {
        return users.keySet();
    }
}
