package org.minerift.ether.util.newnbt.token;

import org.minerift.ether.util.newnbt.token.dyn.CompoundChildrenContent;
import org.minerift.ether.util.newnbt.token.dyn.ListChildrenContent;

import java.util.function.Supplier;

public enum StatelessToken {

    TAG_ID(Token.TAG_ID),
    TAG_NAME(Token.TAG_NAME),
    TAG_PAYLOAD(Token.TAG_PAYLOAD),

    COMPOUND_START(Token.COMPOUND_START),
    COMPOUND_CHILDREN_CONTENT(CompoundChildrenContent::createUnknownSize),
    COMPOUND_END(Token.COMPOUND_END), // writes end tag + indicates end of compound

    LIST_CHILD_TAG_ID(Token.LIST_CHILD_TAG_ID),
    LIST_LENGTH(Token.LIST_LENGTH),
    LIST_START(Token.LIST_START),
    LIST_CHILDREN_CONTENT(ListChildrenContent::new),
    LIST_END(Token.LIST_END),

    ARRAY_CONTENT_BYTE(Token.ARRAY_CONTENT_BYTE),
    ARRAY_CONTENT_INT(Token.ARRAY_CONTENT_INT),
    ARRAY_CONTENT_LONG(Token.ARRAY_CONTENT_LONG),

    TAG_CONTENT_BYTE(Token.TAG_CONTENT_BYTE),
    TAG_CONTENT_SHORT(Token.TAG_CONTENT_SHORT),
    TAG_CONTENT_INT(Token.TAG_CONTENT_INT),
    TAG_CONTENT_LONG(Token.TAG_CONTENT_LONG),
    TAG_CONTENT_FLOAT(Token.TAG_CONTENT_FLOAT),
    TAG_CONTENT_DOUBLE(Token.TAG_CONTENT_DOUBLE),
    TAG_CONTENT_STRING(Token.TAG_CONTENT_STRING),

    ;

    private final Object itemOrSupplier;
    StatelessToken(Token token) {
        this.itemOrSupplier = token;
    }

    StatelessToken(Supplier<Token> tokenSupplier) {
        this.itemOrSupplier = tokenSupplier;
    }

    public Token getToken() {
        return (Token) ((itemOrSupplier instanceof Supplier<?> supplier) ? supplier.get() : itemOrSupplier);
    }

}
