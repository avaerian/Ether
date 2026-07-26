package org.minerift.ether.util.yaml.graph;

import java.util.Collections;
import java.util.List;

public class SequenceNode extends Node {

    private List<Node> seq;

    public SequenceNode(List<Node> seq) {
        this.seq = seq;
    }

    public List<Node> getSequence() {
        return Collections.unmodifiableList(seq);
    }

}
