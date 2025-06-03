package org.minerift.ether.nms;

public class BlockStateNotFoundException extends Exception {

    public BlockStateNotFoundException() {
        super();
    }

    public BlockStateNotFoundException(String message) {
        super(message);
    }

    public BlockStateNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public BlockStateNotFoundException(Throwable cause) {
        super(cause);
    }

    protected BlockStateNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
