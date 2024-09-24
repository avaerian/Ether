package org.minerift.ether.util.newnbt.exceptions;

import java.io.IOException;

// TODO: make this a checked exception?
public class NBTReaderException extends RuntimeException {


    public NBTReaderException() {
        super();
    }

    public NBTReaderException(String message) {
        super(message);
    }

    public NBTReaderException(String message, Throwable cause) {
        super(message, cause);
    }

    public NBTReaderException(Throwable cause) {
        super(cause);
    }

    protected NBTReaderException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
