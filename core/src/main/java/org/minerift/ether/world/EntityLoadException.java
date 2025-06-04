package org.minerift.ether.world;

public class EntityLoadException extends Exception {

    public EntityLoadException() {
    }

    public EntityLoadException(String message) {
        super(message);
    }

    public EntityLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public EntityLoadException(Throwable cause) {
        super(cause);
    }

    public EntityLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
