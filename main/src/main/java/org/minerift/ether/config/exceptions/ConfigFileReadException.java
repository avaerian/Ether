package org.minerift.ether.config.exceptions;

import java.io.IOException;

/**
 * An exception for any config reading error that occurs when parsing
 * @author Avaerian
 */
public class ConfigFileReadException extends IOException {
    public ConfigFileReadException() {}

    public ConfigFileReadException(String message) {
        super(message);
    }

    public ConfigFileReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigFileReadException(Throwable cause) {
        super(cause);
    }
}
