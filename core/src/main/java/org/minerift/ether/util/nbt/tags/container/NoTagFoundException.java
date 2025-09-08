package org.minerift.ether.util.nbt.tags.container;

import org.minerift.ether.util.nbt.NbtException;

public class NoTagFoundException extends NbtException {
    public NoTagFoundException() {
        super();
    }

    public NoTagFoundException(String message) {
        super(message);
    }

    public NoTagFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NoTagFoundException(Throwable cause) {
        super(cause);
    }

    protected NoTagFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
