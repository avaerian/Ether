package org.minerift.ether.nms;

import org.bukkit.Bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NMS {

    private ServerImplVersion serverImplVersion;
    private MinecraftVersion mcVersion;

    public NMS() {

        // TODO: reevaluate MinecraftVersion class and continue abstracting NMS

    }

    public ServerImplVersion getServerImplVersion() {
        throw new UnsupportedOperationException("Unimplemented!");
    }

    // TODO: need to implement
    public MinecraftVersion getMinecraftVersion() {
        if(mcVersion == null) {
            this.mcVersion = new MinecraftVersion(Bukkit.getVersion());
        }
        return mcVersion;
    }

    /*public String getRawServerVersion() {
        if(rawVersion == null) {
            StringBuilder sb = new StringBuilder();
            Matcher matcher = BUKKIT_VERSION_PATTERN.matcher(Bukkit.getVersion());
            while(matcher.find()) {
                matcher.appendReplacement(sb, "");
            }
            matcher.appendTail(sb);
            this.rawVersion = sb.toString();
        }
        return rawVersion;
    }*/



}
