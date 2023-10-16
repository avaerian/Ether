package org.minerift.ether.database.sql.adapters;

import org.minerift.ether.Ether;
import org.minerift.ether.user.EtherUser;
import org.minerift.ether.user.UserManager;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class EtherUsers2UuidsAdapter implements Adapter<Set<EtherUser>, UUID[]> {
    @Override
    public UUID[] adaptTo(Set<EtherUser> obj) {
        return obj.stream().map(EtherUser::getUUID).toArray(UUID[]::new);
    }

    @Override
    public Set<EtherUser> adaptFrom(UUID[] obj) {
        final UserManager users = Ether.getUserManager();
        // TODO: improve this with uuid checks?
        return Arrays.stream(obj)
                .map(uuid -> users.getUser(uuid).get())
                .collect(Collectors.toSet());
    }
}
