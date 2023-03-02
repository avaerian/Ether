package org.minerift.ether.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

// TODO: needs work
public class UserManager {

    private Map<UUID, EtherUser> users;

    public UserManager() {
        this.users = new HashMap<>();
    }

    public Optional<EtherUser> getUser(UUID uuid) {
        return Optional.ofNullable(users.get(uuid));
    }

}
