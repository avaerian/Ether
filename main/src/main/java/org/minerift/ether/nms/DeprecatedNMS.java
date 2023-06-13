package org.minerift.ether.nms;

import org.bukkit.Bukkit;

@Deprecated
public class DeprecatedNMS {

    private NMSVersion nmsVersion;
    private MinecraftVersion nativeVersion;

    private NMSBridge bridge;

    public DeprecatedNMS() {

        this.nativeVersion = getMinecraftVersion();
        this.nmsVersion = getNMSVersion(nativeVersion);
        //this.bridge = Class.forName("org.minerift.ether.nms.v" + DeprecatedNMS);

    }















    // TODO: REFACTOR PLEASE
    public NMSVersion getNMSVersion(MinecraftVersion version) {
        return switch(version.toString()) {
            case "1.8.4":
            case "1.8.5":
            case "1.8.6":
            case "1.8.7":
            case "1.8.8": yield NMSVersion.V1_8_R3;

            case "1.9.0":
            case "1.9.2": yield NMSVersion.V1_9_R1;

            case "1.9.4": yield NMSVersion.V1_9_R2;

            case "1.10.0":
            case "1.10.2": yield NMSVersion.V1_10_R1;

            case "1.11.0":
            case "1.11.1":
            case "1.11.2": yield NMSVersion.V1_11_R1;

            case "1.12.0":
            case "1.12.1":
            case "1.12.2": yield NMSVersion.V1_12_R1;

            case "1.13.0": yield NMSVersion.V1_13_R1;
            case "1.13.1":
            case "1.13.2": yield NMSVersion.V1_13_R2;

            case "1.14.0":
            case "1.14.1":
            case "1.14.2":
            case "1.14.3":
            case "1.14.4": yield NMSVersion.V1_14_R1;

            case "1.15.0":
            case "1.15.1":
            case "1.15.2": yield NMSVersion.V1_15_R1;

            case "1.16.1": yield NMSVersion.V1_16_R1;
            case "1.16.2":
            case "1.16.3": yield NMSVersion.V1_16_R2;
            case "1.16.4":
            case "1.16.5": yield NMSVersion.V1_16_R3;

            case "1.17.0":
            case "1.17.1": yield NMSVersion.V1_17_R1;

            case "1.18.0":
            case "1.18.1": yield NMSVersion.V1_18_R1;
            case "1.18.2": yield NMSVersion.V1_18_R2;

            case "1.19.0":
            case "1.19.1":
            case "1.19.2": yield NMSVersion.V1_19_R1;
            case "1.19.3": yield NMSVersion.V1_19_R2;
            case "1.19.4": yield NMSVersion.V1_19_R3;

            default: yield NMSVersion.UNKNOWN;
        };
    }

    public MinecraftVersion getMinecraftVersion() {
        if(nativeVersion == null) {
            this.nativeVersion = new MinecraftVersion(Bukkit.getVersion());
        }
        return nativeVersion;
    }





}
