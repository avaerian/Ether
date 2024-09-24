package org.minerift.ether.util.newnbt.io;

import org.minerift.ether.util.newnbt.NBT;
import org.minerift.ether.util.newnbt.tags.container.AbstractContainerTag;
import org.minerift.ether.util.newnbt.token.Token;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Stack;

public abstract class NBTContext {

    public final Deque<Token> tokenStack;
    public final Stack<AbstractContainerTag<?>> containerStack;

    public NBTContext() {
        this.tokenStack = new ArrayDeque<>();
        this.containerStack = new Stack<>();
    }

    public abstract String getCurrentTagName();
    public abstract NBT.TagType getCurrentTagType();


}
