package org.minerift.ether.util;

import com.google.common.base.Preconditions;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class Result<V, E> {

    private Object value;
    private ResultType type;

    public Result() {
        this.value = null;
        this.type = ResultType.EMPTY;
    }

    public Result<V, E> ok(V value) {
        Preconditions.checkArgument(type == ResultType.EMPTY, "This result is already of type " + type.name());
        this.value = value;
        this.type = ResultType.OK;
        return this;
    }

    public Result<V, E> err(E err) {
        Preconditions.checkNotNull(err, "Err cannot be null for Result!");
        Preconditions.checkArgument(type == ResultType.EMPTY, "This result is already of type " + type.name());
        this.value = err;
        this.type = ResultType.ERR;
        return this;
    }

    public void handle(Consumer<V> okCallback, Consumer<E> errCallback) {
        // Set null to empty callbacks
        okCallback = okCallback == null ? (ok) -> {} : okCallback;
        errCallback = errCallback == null ? (err) -> {} : errCallback;

        // Handle
        switch(type) {
            case OK -> okCallback.accept(getOk());
            case ERR -> errCallback.accept(getErr());
            case EMPTY -> throw new UnsupportedOperationException("Result is empty and needs to be set!");
        }
    }

    // Returns the success value or default (if an error)
    public V getValueOrDefault(Supplier<V> defaultSupplier) {
        return switch(type) {
            case OK -> getOk();
            case ERR -> defaultSupplier == null ? null : defaultSupplier.get();
            case EMPTY -> throw new UnsupportedOperationException("Result is empty and needs to be set!");
        };
    }

    public V getOk() {
        return (V) value;
    }

    public E getErr() {
        return (E) value;
    }

    public boolean isOk() {
        return type == ResultType.OK;
    }

    public boolean isErr() {
        return type == ResultType.ERR;
    }

    // Runs a callback safely (if not null)
    private void runCallback(Runnable callback) {
        if(callback != null) {
            callback.run();
        }
    }

    public enum ResultType {
        EMPTY,
        OK,
        ERR
    }

}
