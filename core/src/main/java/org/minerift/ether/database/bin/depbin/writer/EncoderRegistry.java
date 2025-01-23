package org.minerift.ether.database.bin.depbin.writer;

import com.google.common.collect.ImmutableMap;
import org.minerift.ether.database.DataType;
import org.minerift.ether.database.PrimitiveType;
import org.minerift.ether.util.fn.IBuilder;
import org.minerift.ether.util.pair.Pair;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;

// Interestingly enough, this class is mutable by default and immutable if desired
public class EncoderRegistry {

    private Map<PrimitiveType, Encoder> encoders;

    protected EncoderRegistry(Map<PrimitiveType, Encoder> encoders) {
        this.encoders = encoders;
    }

    public EncoderRegistry() {
        this(new EnumMap<>(PrimitiveType.class));
    }

    public <T> void register(DataType<T> type, Encoder<T> encoder) {
        try {
            encoders.putIfAbsent(type.getPrimitiveType(), encoder);
        } catch (UnsupportedOperationException ex) {
            throw new UnsupportedOperationException("Encoder registry is immutable!", ex);
        }
    }

    public <T> Encoder<T> getEncoder(DataType<T> type) {
        return getEncoder(type.getPrimitiveType());
    }

    public <T> Encoder<T> getEncoder(PrimitiveType primitive) {
        Encoder<T> encoder = encoders.get(primitive);
        if(encoder == null) {
            throw new NoSuchElementException("No encoder found for " + primitive);
        }
        return encoder;
    }

    public static class Builder implements IBuilder<EncoderRegistry> {

        protected LinkedList<Pair<PrimitiveType, Encoder>> requested;

        public Builder() {
            this.requested = new LinkedList<>();
        }

        public <T> EncoderRegistry.Builder register(DataType<T> type, Encoder<T> encoder) {
            this.requested.add(new Pair<>(type.getPrimitiveType(), encoder));
            return this;
        }

        @Override
        public EncoderRegistry build() {
            ImmutableMap.Builder<PrimitiveType, Encoder> builder = ImmutableMap.builderWithExpectedSize(requested.size());
            for(Pair<PrimitiveType, Encoder> pair : requested) {
                builder.put(pair.getFirst(), pair.getSecond());
            }
            return new EncoderRegistry(builder.build());
        }
    }

}
