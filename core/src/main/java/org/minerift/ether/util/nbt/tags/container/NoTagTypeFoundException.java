package org.minerift.ether.util.nbt.tags.container;

import org.minerift.ether.util.nbt.NbtException;

public class NoTagTypeFoundException extends NbtException {
    public NoTagTypeFoundException() {
        super();
    }

    public NoTagTypeFoundException(String message) {
        super(message);
    }

    public NoTagTypeFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoTagTypeFoundException(Throwable cause) {
        super(cause);
    }

    protected NoTagTypeFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
