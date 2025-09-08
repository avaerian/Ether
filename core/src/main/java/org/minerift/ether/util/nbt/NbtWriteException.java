package org.minerift.ether.util.nbt;

@Deprecated
public class NbtWriteException extends NbtException {
    public NbtWriteException() {
        super();
    }

    public NbtWriteException(String message) {
        super(message);
    }

    public NbtWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public NbtWriteException(Throwable cause) {
        super(cause);
    }

    protected NbtWriteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
