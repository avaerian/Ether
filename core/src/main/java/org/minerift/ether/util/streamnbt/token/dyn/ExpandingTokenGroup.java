package org.minerift.ether.util.streamnbt.token.dyn;

import org.minerift.ether.util.streamnbt.token.StatelessTokenSchema;
import org.minerift.ether.util.streamnbt.token.Token;

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
