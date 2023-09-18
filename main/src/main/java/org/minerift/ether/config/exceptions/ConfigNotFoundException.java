package org.minerift.ether.config.exceptions;

import java.io.IOException;

// TODO: reconsider
public class ConfigNotFoundException extends IOException {
    public ConfigNotFoundException() {
        super();
    }

    public ConfigNotFoundException(String message) {
        super(message);
    }
}
