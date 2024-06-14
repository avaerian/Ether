package org.minerift.ether.util.newnbt;

import org.minerift.ether.util.IBuilder;
import org.minerift.ether.util.nbt.tags.Tag;
import org.minerift.ether.util.newnbt.primitives.IOPrimitive;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

// Represents a queue of tokens ("data types") to read/write
public class TagIOSchem {

    // TODO: change this to be more of a read-only type thing
    private final Deque<Token> tokenQueue;

    public TagIOSchem(Token ... tokens) {
        this.tokenQueue = new ArrayDeque<>(tokens.length);
        pushAll(tokens);
    }

    public void push(Token token) {
        tokenQueue.push(token);
    }

    public void pushAll(Token ... tokens) {
        for(Token tok : tokens) {
            tokenQueue.push(tok);
        }
    }

    public Token poll() {
        return tokenQueue.poll();
    }

    public Token peek() {
        return tokenQueue.peek();
    }


    // TODO: for State, maybe use Stack of TagBuilders? for reading recursive entries in a state machine
    public static class State {
        private NBT.TagType currentTagType;
        private Stack<IBuilder<Tag>> tagStack;
        public Token currentToken;
        public Token nextToken;

        public State() {
            this.currentTagType = null;
            this.tagStack = new Stack<>();
            this.currentToken = Token.TAG_ID; // start by reading tag id
            this.nextToken = null; // TODO
        }

        public void readNext() {
            switch(currentToken) {

            }



            currentToken = nextToken;
        }

        public IBuilder<Tag> peekCurrentTag() {
            return tagStack.peek();
        }

        public IBuilder<Tag> popCurrentTag() {
            return tagStack.pop();
        }

    }

    // current read or write state (active byte(s) selected)
    public enum Token {
        TAG_ID(IOPrimitive.BYTE),

        LIST_CHILD_TAG_ID(IOPrimitive.BYTE),
        LIST_LENGTH(IOPrimitive.INT),

        ARRAY_LENGTH(IOPrimitive.INT),

        ARRAY_CONTENT_BYTE(IOPrimitive.BYTE_ARRAY),
        ARRAY_CONTENT_INT(IOPrimitive.INT_ARRAY),
        ARRAY_CONTENT_LONG(IOPrimitive.LONG_ARRAY),

        STRING_LENGTH(IOPrimitive.SHORT),
        STRING_CONTENT(IOPrimitive.BYTE_ARRAY),

        TAG_CONTENT_BYTE(IOPrimitive.BYTE),
        TAG_CONTENT_SHORT(IOPrimitive.SHORT),
        TAG_CONTENT_INT(IOPrimitive.INT),
        TAG_CONTENT_LONG(IOPrimitive.LONG),
        TAG_CONTENT_FLOAT(IOPrimitive.FLOAT),
        TAG_CONTENT_DOUBLE(IOPrimitive.DOUBLE),

        ;

        private final IOPrimitive<?> primitive;
        Token(IOPrimitive<?> primitive) {
            this.primitive = primitive;
        }

        public IOPrimitive<?> getPrimitive() {
            return primitive;
        }
    }
}
