package org.minerift.ether.util.nbt;

public class NbtException extends Exception {
    public NbtException() {
        super();
    }

    public NbtException(String message) {
        super(message);
    }

    public NbtException(String message, Throwable cause) {
        super(message, cause);
    }

    public NbtException(Throwable cause) {
        super(cause);
    }

    protected NbtException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
