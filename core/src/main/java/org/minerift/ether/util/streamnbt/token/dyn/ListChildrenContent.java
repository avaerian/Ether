package org.minerift.ether.util.streamnbt.token.dyn;

import org.minerift.ether.util.streamnbt.NBT;
import org.minerift.ether.util.streamnbt.token.StatelessTokenSchema;

public class ListChildrenContent implements ExpandingTokenGroup {

    private int listLength;
    private StatelessTokenSchema payloadSchema;
    private int current;

    public ListChildrenContent() {
        this.listLength = 0;
        this.payloadSchema = null;
        this.current = 0;
    }

    public void setChildType(NBT.TagType childType) {
        this.payloadSchema = childType.getPayloadSchema();
    }

    public void setListLength(int length) {
        this.listLength = length;
    }

    public boolean isReady() {
        return payloadSchema != null;
    }

    public int current() {
        return current;
    }

    /*@Override
    public Token readNext() {
        //Token token = tokens[current++ % tokens.length].getToken();
        //System.out.println("readNext ListContentTokens: " + token.getName());
        //return token;
    }*/

    @Override
    public StatelessTokenSchema readNextSchema() {
        current++;
        return payloadSchema;
    }

    @Override
    public boolean hasRemainingTokens() {
        return current >= 0 && current < listLength;
    }

    @Override
    public String getName() {
        return "LIST_CHILDREN_CONTENT";
    }

    @Override
    public String toString() {
        return "ListChildrenContent{" +
                "listLength=" + listLength +
                ", payloadSchema=" + payloadSchema +
                ", current=" + current +
                '}';
    }
}
