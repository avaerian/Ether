package org.minerift.ether.util.streamnbt.token;

import org.minerift.ether.util.streamnbt.io.primitives.*;

// Each Token object contains info about the token type (name, bytes selected (?), etc.)
@Deprecated(forRemoval = true)
public interface Token {

    TagIdToken TAG_ID = new TagIdToken();
    TagNameToken TAG_NAME = new TagNameToken();
    GenericTagPayloadToken TAG_PAYLOAD = new GenericTagPayloadToken();

    ListChildTagIdToken LIST_CHILD_TAG_ID = new ListChildTagIdToken(); // preliminary token
    ListLengthToken LIST_LENGTH = new ListLengthToken(); // preliminary token
    ListStartToken LIST_START = new ListStartToken();
    ListEndToken LIST_END = new ListEndToken();

    // TAG_ID + TAG_NAME_LENGTH + TAG_NAME_CONTENT + LIST_CHILD_TAG_ID + LIST_LENGTH + (append TAG_CONTENT_X tokens)
    CompoundStartToken COMPOUND_START = new CompoundStartToken();
    CompoundEndToken COMPOUND_END = new CompoundEndToken();

    // Tag values

    ArrayContentByteToken ARRAY_CONTENT_BYTE = new ArrayContentByteToken();
    ArrayContentIntToken ARRAY_CONTENT_INT = new ArrayContentIntToken();
    ArrayContentLongToken ARRAY_CONTENT_LONG = new ArrayContentLongToken();

    TagContentByteToken TAG_CONTENT_BYTE = new TagContentByteToken();
    TagContentShortToken TAG_CONTENT_SHORT = new TagContentShortToken();
    TagContentIntToken TAG_CONTENT_INT = new TagContentIntToken();
    TagContentLongToken TAG_CONTENT_LONG = new TagContentLongToken();
    TagContentFloatToken TAG_CONTENT_FLOAT = new TagContentFloatToken();
    TagContentDoubleToken TAG_CONTENT_DOUBLE = new TagContentDoubleToken();
    TagContentStringToken TAG_CONTENT_STRING = new TagContentStringToken();


    String getName();


    // current read or write state (active byte(s) selected)
    interface IOToken extends Token {
        IOPrimitive<?> bytesSelected();
    }

    record GenericTagPayloadToken() implements Token {
        @Override
        public String getName() {
            return "TAG_ID_NAME_PAYLOAD";
        }
    }

    interface ContainerStartToken extends Token { }
    interface ContainerEndToken extends Token { }

    record CompoundStartToken() implements Token, ContainerStartToken {
        @Override
        public String getName() {
            return "COMPOUND_START";
        }
    }

    record CompoundEndToken() implements IOToken, ContainerEndToken {
        @Override
        public String getName() {
            return "COMPOUND_END";
        }

        // NOTE: only used for writing. This token is meaningless when reading.
        @Override
        public ByteIOPrimitive bytesSelected() {
            return IOPrimitive.BYTE;
        }
    }

    record ListStartToken() implements Token, ContainerStartToken {
        @Override
        public String getName() {
            return "LIST_START";
        }
    }

    record ListEndToken() implements Token, ContainerEndToken {
        @Override
        public String getName() {
            return "LIST_END";
        }
    }


    public record TagIdToken() implements IOToken {
        @Override
        public ByteIOPrimitive bytesSelected() {
            return IOPrimitive.BYTE;
        }

        @Override
        public String getName() {
            return "TAG_ID";
        }
    }

    public record ListChildTagIdToken() implements IOToken {
        @Override
        public ByteIOPrimitive bytesSelected() {
            return IOPrimitive.BYTE;
        }

        @Override
        public String getName() {
            return "LIST_CHILD_TAG_ID";
        }
    }

    public record ListLengthToken() implements IOToken {
        @Override
        public IntIOPrimitive bytesSelected() {
            return IOPrimitive.INT;
        }

        @Override
        public String getName() {
            return "LIST_LENGTH";
        }
    }

    public record ArrayContentByteToken() implements IOToken {
        @Override
        public ByteArrayIOPrimitive bytesSelected() {
            return IOPrimitive.BYTE_ARRAY;
        }

        @Override
        public String getName() {
            return "ARRAY_CONTENT_BYTE";
        }
    }

    public record ArrayContentIntToken() implements IOToken {
        @Override
        public IntArrayIOPrimitive bytesSelected() {
            return IOPrimitive.INT_ARRAY;
        }

        @Override
        public String getName() {
            return "ARRAY_CONTENT_INT";
        }
    }

    public record ArrayContentLongToken() implements IOToken {
        @Override
        public LongArrayIOPrimitive bytesSelected() {
            return IOPrimitive.LONG_ARRAY;
        }

        @Override
        public String getName() {
            return "ARRAY_CONTENT_LONG";
        }
    }

    public interface StringIOToken extends IOToken {
        @Override
        default StringIOPrimitive bytesSelected() {
            return IOPrimitive.STRING;
        }
    }

    public static class TagContentStringToken implements StringIOToken {
        @Override
        public String getName() {
            return "TAG_CONTENT_STRING";
        }
    }
    public static class TagNameToken implements StringIOToken {
        @Override
        public String getName() {
            return "TAG_NAME";
        }
    }

    // Simple Tag Content Tokens

    public record TagContentByteToken() implements IOToken {
        @Override
        public ByteIOPrimitive bytesSelected() {
            return IOPrimitive.BYTE;
        }

        @Override
        public String getName() {
            return "TAG_CONTENT_BYTE";
        }
    }

    public record TagContentShortToken() implements IOToken {
        @Override
        public ShortIOPrimitive bytesSelected() {
            return IOPrimitive.SHORT;
        }

        @Override
        public String getName() {
            return "TAG_CONTENT_SHORT";
        }
    }

    public record TagContentIntToken() implements IOToken {
        @Override
        public IntIOPrimitive bytesSelected() {
            return IOPrimitive.INT;
        }

        @Override
        public String getName() {
            return "TAG_CONTENT_INT";
        }
    }

    public record TagContentLongToken() implements IOToken {
        @Override
        public LongIOPrimitive bytesSelected() {
            return IOPrimitive.LONG;
        }

        @Override
        public String getName() {
            return "TAG_CONTENT_LONG";
        }
    }

    public record TagContentFloatToken() implements IOToken {
        @Override
        public FloatIOPrimitive bytesSelected() {
            return IOPrimitive.FLOAT;
        }

        @Override
        public String getName() {
            return "TAG_CONTENT_FLOAT";
        }
    }

    public record TagContentDoubleToken() implements IOToken {
        @Override
        public DoubleIOPrimitive bytesSelected() {
            return IOPrimitive.DOUBLE;
        }

        @Override
        public String getName() {
            return "TAG_CONTENT_DOUBLE";
        }
    }

}
