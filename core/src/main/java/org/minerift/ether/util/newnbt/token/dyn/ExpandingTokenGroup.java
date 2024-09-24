package org.minerift.ether.util.newnbt.token.dyn;

import org.minerift.ether.util.newnbt.token.StatelessTokenSchema;
import org.minerift.ether.util.newnbt.token.Token;

// Represents a lazily-loaded group of tokens
// Token is persistent until appropriate state is reached
// TODO: refactor to ExpandingToken
public interface ExpandingTokenGroup extends Token {

    StatelessTokenSchema readNextSchema();

    boolean hasRemainingTokens();

    /*
    public LazyTokenGroup {
        Preconditions.checkNotNull(tokens);
        Preconditions.checkArgument(tokens.length > 0);
    }*/
}
