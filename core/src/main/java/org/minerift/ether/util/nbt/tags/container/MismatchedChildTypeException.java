package org.minerift.ether.util.nbt.tags.container;

import org.minerift.ether.util.nbt.NbtException;

public class MismatchedChildTypeException extends NbtException {
    public MismatchedChildTypeException() {
        super();
    }

    public MismatchedChildTypeException(String message) {
        super(message);
    }

    public MismatchedChildTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public MismatchedChildTypeException(Throwable cause) {
        super(cause);
    }

    protected MismatchedChildTypeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
