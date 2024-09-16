package org.minerift.ether.schematic;

import java.io.IOException;

public class SchematicFileReadException extends IOException {
    public SchematicFileReadException() {
        super();
    }

    public SchematicFileReadException(String message) {
        super(message);
    }

    public SchematicFileReadException(String message, Throwable cause) {
        super(message, cause);
    }

    public SchematicFileReadException(Throwable cause) {
        super(cause);
    }
}
