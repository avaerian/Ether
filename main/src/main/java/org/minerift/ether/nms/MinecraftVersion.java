package org.minerift.ether.nms;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ComparisonChain;
import org.jetbrains.annotations.NotNull;

import java.util.function.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Reference for additional features for the MinecraftVersion class:
// https://github.com/dmulloy2/ProtocolLib/blob/master/src/main/java/com/comphenix/protocol/utility/MinecraftVersion.java#L396
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

    public boolean isGreaterThan(MinecraftVersion other) {

        if(equals(other)) return false;

        // This is done to evaluate a more complex set of rules while keeping the API clean
        return greaterThanCheckChained(major, other.getMajor(),
                () -> greaterThanCheckChained(minor, other.getMinor(),
                        () -> greaterThanCheckChained(patch, other.getPatch(), null)
                )
        );
    }

    public boolean isLessThan(MinecraftVersion other) {

        if(equals(other)) return false;

        return lessThanCheckChained(major, other.getMajor(),
                () -> lessThanCheckChained(minor, other.getMinor(),
                        () -> lessThanCheckChained(patch, other.getPatch(), null)
                )
        );
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinecraftVersion version = (MinecraftVersion) o;
        return major == version.major && minor == version.minor && patch == version.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(major, minor, patch);
    }

    @Override
    public String toString() {
        return "(%d.%d.%d)".formatted(major, minor, patch);
    }

    @Override
    public int compareTo(@NotNull MinecraftVersion o) {
        return ComparisonChain.start()
                .compare(major, o.getMajor())
                .compare(minor, o.getMinor())
                .compare(patch, o.getPatch())
                .result();
    }


    // Utility methods

    private String[] parseVersion(String version) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = VERSION_PATTERN.matcher(version);
        while(matcher.find()) {
            matcher.appendReplacement(sb, "");
        }
        matcher.appendTail(sb);
        return sb.toString().split("\\.");
    }
    private boolean greaterThanCheckChained(int i, int other, BooleanSupplier chain) {
        // If major is greater, return true
        // If major is less than, return false
        // Else, majors are equal -> continue down chain
        if(i > other) return true;
        if(i < other) return false;
        return chain == null ? false : chain.getAsBoolean();
    }

    private boolean lessThanCheckChained(int i, int other, BooleanSupplier chain) {
        // If major is less than, return true
        // If major is greater, return false
        // Else, majors are equal -> continue down chain
        if(i < other) return true;
        if(i > other) return false;
        return chain == null ? false : chain.getAsBoolean();
    }
}
