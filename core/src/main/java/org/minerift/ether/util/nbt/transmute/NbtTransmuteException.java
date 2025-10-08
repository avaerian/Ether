package org.minerift.ether.util.nbt.transmute;

import org.minerift.ether.util.nbt.NbtException;

public class NbtTransmuteException extends NbtException {
    public NbtTransmuteException() {
        super();
    }

    public NbtTransmuteException(String message) {
        super(message);
    }

    public NbtTransmuteException(String message, Throwable cause) {
        super(message, cause);
    }

    public NbtTransmuteException(Throwable cause) {
        super(cause);
    }

    protected NbtTransmuteException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
