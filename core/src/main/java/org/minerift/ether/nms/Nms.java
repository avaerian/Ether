package org.minerift.ether.nms;

import org.minerift.ether.util.BukkitUtils;

import java.lang.reflect.InvocationTargetException;

public final class Nms {

    public static NmsAccess createAccess() { // TODO: throw exceptions for unsupported/unknown versions
        NmsVersion version = NmsVersion.from(BukkitUtils.getImplVersionStr());
        return createAccess(version);
    }

    public static NmsAccess createAccess(NmsVersion version) {
        try {
            return loadNmsImpl(NmsAccess.class, "NMSAccessImpl", version);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException ex) {
            throw new RuntimeException("Failed to load NMS impl", ex);
        }
    }

    private static <T> T loadNmsImpl(Class<T> iface, String implName, NmsVersion version) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName("org.minerift.ether.nms." + version.asPackageString() + "." + implName);
        return iface.cast(clazz.getConstructor().newInstance());
    }

    private Nms() {}

}
