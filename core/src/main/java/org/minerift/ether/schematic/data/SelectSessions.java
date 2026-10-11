package org.minerift.ether.schematic.data;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import org.jetbrains.annotations.UnknownNullability;
import org.minerift.ether.math.Vec3i;
import org.minerift.ether.user.EtherUser;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * Handles selection sessions for users.
 * The internal min and max BoundingBox stores may differ from a selection Session,
 * thus a Session can store the world coords and the resulting BoundingBox.
 * The BoundingBox will not be created until explicitly requested due to the
 * immutablility of BoundingBox with thread-safety in-mind. Selection Session's
 * provide the mutability and potential asynchronous access to modify bounds
 * before creating a BoundingBox.
 * <p>
 * Sessions allow for the storage of world coords before either creating one
 * or more BoundingBox's, or cancelling the selection altogether.
 */
public class SelectSessions {

    // sessions will persist across different worlds
    // sessions can either be popped by:
    // - user logs off server
    // - session expires (if not set to -1)
    // - user saves selection (unless persisted with -p flag)


    private static final Supplier<Object2ObjectMap<UUID, Selection>> STORE_GEN = Object2ObjectRBTreeMap::new;
    private Object2ObjectMap<UUID, Selection> store;

    public SelectSessions() {
        this(Object2ObjectMaps.emptyMap());
    }

    public SelectSessions(Object2ObjectMap<UUID, Selection> store) {
        this.store = store;
    }

    public Object2ObjectMap<UUID, Selection> getStorage() {
        return Object2ObjectMaps.unmodifiable(store);
    }

    public Object2ObjectMap<UUID, Selection> getStorageMut() {
        return store;
    }

    public Selection selectPos1(EtherUser user, Vec3i pos) {
        return selectPos1(user.getUUID(), pos);
    }

    public Selection selectPos1(UUID uuid, Vec3i pos) {
        if(store == Object2ObjectMaps.EMPTY_MAP) {
            store = STORE_GEN.get();
        }
        Selection sess = store.computeIfAbsent(uuid, (__) -> new Selection());
        sess.pos1 = pos;
        return sess;
    }

    public Selection selectPos2(EtherUser user, Vec3i pos) {
        return selectPos2(user.getUUID(), pos);
    }

    public Selection selectPos2(UUID uuid, Vec3i pos) {
        if(store == Object2ObjectMaps.EMPTY_MAP) {
            store = STORE_GEN.get();
        }
        Selection sess = store.computeIfAbsent(uuid, (__) -> new Selection());
        sess.pos2 = pos;
        return sess;
    }

    public @UnknownNullability Selection selectFor(EtherUser user) {
        return selectFor(user.getUUID());
    }

    public @UnknownNullability Selection selectFor(UUID uuid) {
        return store.get(uuid);
    }

    public Selection pop(EtherUser user) {
        return pop(user.getUUID());
    }

    public Selection pop(UUID uuid) {
        return store.remove(uuid);
    }

    public void push(EtherUser user, Selection sess) {
        store.put(user.getUUID(), sess);
    }

    public static class Selection {
        public volatile Vec3i pos1, pos2;

        public Selection() {
            this.pos1 = this.pos2 = null;
        }

        public Selection(Vec3i pos1, Vec3i pos2) {
            this.pos1 = pos1;
            this.pos2 = pos2;
        }

        public /*synchronized*/ BoundingBox box() {
            if(pos1 == null)
                throw new IllegalStateException("pos1 is null");
            if(pos2 == null)
                throw new IllegalStateException("pos2 is null");
            return new BoundingBox(pos1, pos2);
        }
    }

}
