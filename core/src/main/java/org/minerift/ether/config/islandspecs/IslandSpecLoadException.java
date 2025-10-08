package org.minerift.ether.config.islandspecs;

public class IslandSpecLoadException extends Exception {
    public IslandSpecLoadException() {
        super();
    }

    public IslandSpecLoadException(String message) {
        super(message);
    }

    public IslandSpecLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public IslandSpecLoadException(Throwable cause) {
        super(cause);
    }

    protected IslandSpecLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
