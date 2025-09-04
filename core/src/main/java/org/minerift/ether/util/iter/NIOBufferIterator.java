package org.minerift.ether.util.iter;

import com.google.common.collect.AbstractIterator;
import org.jetbrains.annotations.Nullable;

import java.nio.Buffer;
import java.util.function.Function;

public abstract class NIOBufferIterator<B extends Buffer, T> extends AbstractIterator<T> {

    protected final B buffer;
    protected final Function<B, T> bufferReader;
    protected final T invalidSym;

    // Be aware that the buffer being used will be the one controlled (no views or copies created)
    public NIOBufferIterator(B buffer, Function<B, T> bufferReader, int startPos, T invalidSym) {
        this.buffer = buffer;
        this.bufferReader = bufferReader;
        this.invalidSym = invalidSym;
        buffer.rewind();
        buffer.position(startPos);
    }

    public NIOBufferIterator(B buffer, Function<B, T> bufferReader, T invalidSym) {
        this(buffer, bufferReader, 0, invalidSym);
    }

    @Nullable
    @Override
    protected T computeNext() {
        if(!buffer.hasRemaining()) {
            endOfData();
            return invalidSym;
        }
        return bufferReader.apply(buffer);
    }
}
