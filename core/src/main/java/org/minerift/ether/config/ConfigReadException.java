package org.minerift.ether.config;

import java.io.IOException;

/**
 * An exception for any config reading error that occurs when parsing
 * @author Avaerian
 */
public class ConfigReadException extends IOException {
    public ConfigReadException() {}

    public ConfigReadException(String message) {
        super(message);
    }

    public ConfigReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigReadException(Throwable cause) {
        super(cause);
    }
}
