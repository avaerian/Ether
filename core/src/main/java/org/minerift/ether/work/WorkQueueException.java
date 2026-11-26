package org.minerift.ether.work;

public class WorkQueueException extends Exception {
    public WorkQueueException() {
        super();
    }

    public WorkQueueException(String message) {
        super(message);
    }

    public WorkQueueException(String message, Throwable cause) {
        super(message, cause);
    }

    public WorkQueueException(Throwable cause) {
        super(cause);
    }

    protected WorkQueueException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
