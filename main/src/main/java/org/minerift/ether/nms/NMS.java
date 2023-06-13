package org.minerift.ether.nms;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;

import java.lang.reflect.InvocationTargetException;

public class NMS {

    private String implVersion;
    private DeprecatedNMSBridge bridge;

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

    public void resetChunk(Chunk chunk) {
        bridge.fastResetChunk(chunk);
    }

    private String getImplVersion() {
        if(implVersion == null) {
            String version = Bukkit.getServer().getClass().getPackageName();
            implVersion = version.substring(version.lastIndexOf('.') + 2);
        }
        return implVersion;
    }

    private DeprecatedNMSBridge getBridge(String version) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<?> clazz = Class.forName("org.minerift.ether.nms.v" + version + ".NMSBridgeImpl");
        return (DeprecatedNMSBridge) clazz.getConstructor().newInstance();
    }

}
