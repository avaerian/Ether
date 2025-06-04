package org.minerift.ether.util.streamnbt.token.dyn;

import org.minerift.ether.util.streamnbt.token.StatelessTokenSchema;

// TODO: refactor as non-"ExpandingTokenGroup" class?
public class CompoundChildrenContent implements ExpandingTokenGroup {

    private int knownSize;
    private int current;

    public static CompoundChildrenContent create(int knownSize) {
        return new CompoundChildrenContent(knownSize);
    }

    public static CompoundChildrenContent createUnknownSize() {
        return create(Integer.MAX_VALUE);
    }

    private CompoundChildrenContent(int knownSize) {
        this.knownSize = knownSize;
        this.current = 0;
    }

    public void setKnownSize(int knownSize) {
        this.knownSize = knownSize;
    }

    public int getKnownSize() {
        return knownSize;
    }

    public int current() {
        return current;
    }

    @Override
    public String getName() {
        return "COMPOUND_CHILDREN_CONTENT";
    }

    @Override
    public StatelessTokenSchema readNextSchema() {
        current++;
        return StatelessTokenSchema.GENERIC_TAG_ID_NAME_PAYLOAD;
    }

    @Override
    public boolean hasRemainingTokens() {
        return current < knownSize;
    }

    @Override
    public String toString() {
        return "CompoundChildrenContent{}";
    }
}
