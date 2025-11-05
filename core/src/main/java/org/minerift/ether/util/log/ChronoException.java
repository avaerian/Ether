package org.minerift.ether.util.log;

public class ChronoException extends RuntimeException {
    public ChronoException() {
    }

    public ChronoException(String message) {
        super(message);
    }

    public ChronoException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChronoException(Throwable cause) {
        super(cause);
    }

    public ChronoException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}