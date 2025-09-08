package org.minerift.ether.util.nbt.snbt;

public class UnexpectedTokenException extends SnbtException {
    public UnexpectedTokenException() {
    }

    public UnexpectedTokenException(String message) {
        super(message);
    }

    public UnexpectedTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnexpectedTokenException(Throwable cause) {
        super(cause);
    }

    public UnexpectedTokenException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
