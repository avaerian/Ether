package org.minerift.ether.database.nusql.adapters;

import org.minerift.ether.user.EtherUser;

import java.util.*;

public class EtherUsers2UuidsAdapter implements Adapter<List<EtherUser>, UUID[]> {

    @Override
    public UUID[] adaptTo(List<EtherUser> obj) {
        return obj.stream().map(EtherUser::getUUID).toArray(UUID[]::new); // TODO: address null users !!!!!!
    }

    @Override
    public List<EtherUser> adaptFrom(UUID[] obj) {

        // TODO: debug DB API
        List<EtherUser> users = new ArrayList<>(obj.length);
        for(UUID uuid : obj) {
            var user = EtherUser.builder().setUUID(uuid).build();
            users.add(user);
        }
        return users;

        /*
        // TODO: use this code later?
        final UserManager users = Ether.getUserManager();
        return Arrays.stream(obj)
                .map(uuid -> users.getUser(uuid).get())
                .collect(Collectors.toSet());
         */
    }
}
