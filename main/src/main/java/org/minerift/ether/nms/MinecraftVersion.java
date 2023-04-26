package org.minerift.ether.nms;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MinecraftVersion implements Comparable<MinecraftVersion> {

    private static final Pattern VERSION_PATTERN = Pattern.compile("[^\\.|\\d]");

    private int major, minor, patch;

    public MinecraftVersion(String version) {
        Preconditions.checkNotNull(version);
        String[] splitVersion = parseVersion(version);
        Preconditions.checkArgument(splitVersion.length == 3, "Invalid Minecraft version attempted to be parsed!");

        setWithValidation(
                Integer.parseInt(splitVersion[0]),
                Integer.parseInt(splitVersion[1]),
                Integer.parseInt(splitVersion[2])
        );
    }

    public MinecraftVersion(int major, int minor, int patch) {
        setWithValidation(major, minor, patch);
    }

    private void setWithValidation(int major, int minor, int patch) {
        Preconditions.checkArgument(major >= 0, "Major version must be positive!");
        Preconditions.checkArgument(minor >= 0, "Minor version must be positive!");
        Preconditions.checkArgument(patch >= 0, "Patch version must be positive!");

        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    // Score used for comparing
    public int getCompareScore() {
        long score = (major * 10000L) + (minor * 100L) + (patch * 1L) - 10000L;
        return (int)score;
    }

    private String[] parseVersion(String version) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = VERSION_PATTERN.matcher(version);
        while(matcher.find()) {
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        return sb.toString().split("\\.");
    }

    @Override
    public String toString() {
        return "(%d.%d.%d)".formatted(major, minor, patch);
    }

    @Override
    public int compareTo(@NotNull MinecraftVersion o) {
        return getCompareScore() - o.getCompareScore();
    }
}
