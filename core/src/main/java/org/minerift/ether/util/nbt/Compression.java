package org.minerift.ether.util.nbt;

public enum Compression {
    NONE,
    GZIP,
    ZLIB,

    ;

    // references https://isc.sans.edu/diary/25182.
    // tentatively check for zlib/gzip compression.
    // big-endian magic
    static Compression fromMagic(short magic) {
        final Compression cmps;
        if(magic == 0x7801 /* zlib no/low compression */
                || magic == 0x789c /* zlib default compression */
                || magic == 0x78da /* zlib best compression */) {
            cmps = Compression.ZLIB;
        } else if(magic == 0x1f8b) { /* gzip compression */
            cmps = Compression.GZIP;
        } else {
            cmps = Compression.NONE;
        }
        return cmps;
    }
}
