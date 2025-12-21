package org.minerift.ether.util.yaml.graph;

public class Parser {

    private String str;
    public Parser(String str) {
        this.str = str;
    }



    public enum Style {
        BLOCK,
        FLOW,
        AUTO
    }

}
