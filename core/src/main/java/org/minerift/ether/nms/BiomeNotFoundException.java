package org.minerift.ether.nms;

public class BiomeNotFoundException extends Exception {
    public BiomeNotFoundException() {
        super();
    }

    public BiomeNotFoundException(String message) {
        super(message);
    }

    public BiomeNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BiomeNotFoundException(Throwable cause) {
        super(cause);
    }

    protected BiomeNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
