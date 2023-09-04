package org.minerift.ether.util;

import com.google.common.base.Preconditions;

import java.util.function.Consumer;

public class Result<V, E> {

    private Object value;
    private ResultType type;

    public Result() {
        this.value = null;
        this.type = ResultType.EMPTY;
    }

    public void ok(V value) {
        Preconditions.checkArgument(type == ResultType.EMPTY, "This result is already of type " + type.name());
        this.value = value;
        this.type = ResultType.OK;
    }

    public void err(E err) {
        Preconditions.checkNotNull(err, "Err cannot be null for Result!");
        Preconditions.checkArgument(type == ResultType.EMPTY, "This result is already of type " + type.name());
        this.value = err;
        this.type = ResultType.ERR;
    }

    public void handle(Consumer<V> okCallback, Consumer<E> errCallback) {
        switch(type) {
            case OK -> okCallback.accept((V) value);
            case ERR -> errCallback.accept((E) value);
            case EMPTY -> throw new UnsupportedOperationException("Result is empty and needs to be set!");
        }
    }

    public enum ResultType {
        EMPTY,
        OK,
        ERR
    }

}
