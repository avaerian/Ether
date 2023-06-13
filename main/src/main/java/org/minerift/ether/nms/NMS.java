package org.minerift.ether.nms;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.lang.reflect.InvocationTargetException;

public class NMS {

    private String implVersion;
    private NMSBridge bridge;

    public NMS() {
        this.implVersion = getImplVersion();

        // Attempt to load bridge
        try {
            this.bridge = getBridge(implVersion);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearChunk(Chunk chunk) {
        bridge.fastClearChunk(chunk);
    }

    private String getImplVersion() {
        if(implVersion == null) {
            String version = Bukkit.getServer().getClass().getPackageName();
            implVersion = version.substring(version.lastIndexOf('.') + 2);
        }
        return implVersion;
    }

    private NMSBridge getBridge(String version) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName("org.minerift.ether.nms.v" + version + ".NMSBridgeImpl");
        return (NMSBridge) clazz.getConstructor().newInstance();
    }

}
