package org.minerift.ether.user;

import java.util.*;

// TODO: needs work
public class UserManager {

    private Map<UUID, EtherUser> users;

    public UserManager() {
        this.users = new HashMap<>();
    }

    // Returns whether the user was registered successfully
    public boolean register(EtherUser user) {
        return users.putIfAbsent(user.getUUID(), user) == null;
    }

    public Optional<EtherUser> getUser(UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

    public List<EtherUser> getUsers(Collection<UUID> ids) {
        List<EtherUser> result = new ArrayList<>(ids.size());
        for(UUID id : ids) {
            result.add(users.get(id));
        }
        return result;
    }

    public Set<UUID> getKeySet() {
        return users.keySet();
    }
}
