package org.minerift.ether.util.nbt.tags.container;

import org.minerift.ether.util.nbt.NbtException;

public class MismatchedTypeException extends NbtException {
    public MismatchedTypeException() {
        super();
    }

    public MismatchedTypeException(String message) {
        super(message);
    }

    public MismatchedTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MismatchedTypeException(Throwable cause) {
        super(cause);
    }

    protected MismatchedTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
