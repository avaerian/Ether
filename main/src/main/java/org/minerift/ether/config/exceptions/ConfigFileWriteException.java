package org.minerift.ether.config.exceptions;

import java.io.IOException;

public class ConfigFileWriteException extends IOException {

    public ConfigFileWriteException() {
    }

    public ConfigFileWriteException(String message) {
        super(message);
    }

    public ConfigFileWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigFileWriteException(Throwable cause) {
        super(cause);
    }
}
