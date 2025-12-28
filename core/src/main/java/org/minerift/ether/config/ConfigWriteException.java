package org.minerift.ether.config;

import java.io.IOException;

public class ConfigWriteException extends IOException {

    public ConfigWriteException() {
    }

    public ConfigWriteException(String message) {
        super(message);
    }

    public ConfigWriteException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigWriteException(Throwable cause) {
        super(cause);
    }
}
