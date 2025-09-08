package org.minerift.ether.util.nbt.snbt;

import org.minerift.ether.util.nbt.NbtException;

public class SnbtException extends NbtException {
    public SnbtException() {
        super();
    }

    public SnbtException(String message) {
        super(message);
    }

    public SnbtException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnbtException(Throwable cause) {
        super(cause);
    }

    protected SnbtException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
