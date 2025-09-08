package org.minerift.ether.util.nbt.snbt;

public class SnbtReadException extends SnbtException {
    public SnbtReadException() {
        super();
    }

    public SnbtReadException(String message) {
        super(message);
    }

    public SnbtReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public SnbtReadException(Throwable cause) {
        super(cause);
    }

    protected SnbtReadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
