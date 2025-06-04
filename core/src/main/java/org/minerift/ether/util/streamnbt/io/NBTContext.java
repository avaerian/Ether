package org.minerift.ether.util.streamnbt.io;

import org.minerift.ether.util.streamnbt.NBT;
import org.minerift.ether.util.nunbt.tags.container.AbstractContainerTag;
import org.minerift.ether.util.streamnbt.token.Token;

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
