package org.minerift.ether.util.nbt;

public class NbtReadException extends NbtException {
    public NbtReadException() {
        super();
    }

    public NbtReadException(String message) {
        super(message);
    }

    public NbtReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public NbtReadException(Throwable cause) {
        super(cause);
    }

    protected NbtReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
