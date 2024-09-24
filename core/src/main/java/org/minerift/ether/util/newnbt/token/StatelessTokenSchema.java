package org.minerift.ether.util.newnbt.token;

import static org.minerift.ether.util.newnbt.token.StatelessToken.*;

public enum StatelessTokenSchema {

    GENERIC_TAG_ID_NAME_PAYLOAD(TAG_ID, TAG_NAME, TAG_PAYLOAD),
    BYTE_TAG_VALUE(TAG_CONTENT_BYTE),
    SHORT_TAG_VALUE(TAG_CONTENT_SHORT),
    INT_TAG_VALUE(TAG_CONTENT_INT),
    LONG_TAG_VALUE(TAG_CONTENT_LONG),
    FLOAT_TAG_VALUE(TAG_CONTENT_FLOAT),
    DOUBLE_TAG_VALUE(TAG_CONTENT_DOUBLE),
    BYTE_ARRAY_VALUE(ARRAY_CONTENT_BYTE),
    INT_ARRAY_VALUE(ARRAY_CONTENT_INT),
    LONG_ARRAY_VALUE(ARRAY_CONTENT_LONG),
    STRING_TAG_VALUE(TAG_CONTENT_STRING),
    LIST_TAG_VALUE( LIST_START, LIST_CHILD_TAG_ID, LIST_LENGTH, LIST_CHILDREN_CONTENT, LIST_END ),
    COMPOUND_TAG_VALUE( COMPOUND_START, COMPOUND_CHILDREN_CONTENT, COMPOUND_END )

    ;

    private final StatelessToken[] tokens;

    StatelessTokenSchema(StatelessToken... tokens) {
        this.tokens = tokens;
    }

    public StatelessToken[] getSchema() {
        return tokens;
    }

    public int size() {
        return tokens.length;
    }

}
