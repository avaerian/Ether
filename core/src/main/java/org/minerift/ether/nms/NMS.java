package org.minerift.ether.nms;

import org.minerift.ether.util.BukkitUtils;

import java.lang.reflect.InvocationTargetException;

public final class NMS {

    public static NMSAccess createAccess() { // TODO: handle unsupported/unknown versions
        NMSVersion version = NMSVersion.from(BukkitUtils.getImplVersionStr());
        return createAccess(version);
    }

    public static NMSAccess createAccess(NMSVersion version) {
        try {
            return loadNmsImpl(NMSAccess.class, "NMSAccessImpl", version);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException ex) {
            throw new RuntimeException("Failed to load NMS impl", ex);
        }
    }

    private static <T> T loadNmsImpl(Class<T> iface, String implName, NMSVersion version) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName("org.minerift.ether.nms." + version.asPackageString() + "." + implName);
        return iface.cast(clazz.getConstructor().newInstance());
    }

    private NMS() {}

}
