package org.minerift.ether.schematic;

import java.io.IOException;

public class SchematicReadException extends IOException {
    public SchematicReadException() {
        super();
    }

    public SchematicReadException(String message) {
        super(message);
    }

    public SchematicReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchematicReadException(Throwable cause) {
        super(cause);
    }
}
