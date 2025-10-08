package org.minerift.ether.island.spawn;

public class SpawnStrategyLoadException extends Exception {
    public SpawnStrategyLoadException() {
        super();
    }

    public SpawnStrategyLoadException(String message) {
        super(message);
    }

    public SpawnStrategyLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public SpawnStrategyLoadException(Throwable cause) {
        super(cause);
    }

    protected SpawnStrategyLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
