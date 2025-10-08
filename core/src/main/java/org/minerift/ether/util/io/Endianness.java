package org.minerift.ether.util.io;

public enum Endianness {
    LITTLE_ENDIAN,
    BIG_ENDIAN,


    ;


    public boolean isLittleEndian() {
        return this == LITTLE_ENDIAN;
    }

    public boolean isBigEndian() {
        return this == BIG_ENDIAN;
    }
}
