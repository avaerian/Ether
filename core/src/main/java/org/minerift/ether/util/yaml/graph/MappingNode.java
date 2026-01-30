package org.minerift.ether.util.yaml.graph;

import java.util.HashMap;
import java.util.Map;

public class MappingNode extends Node {

    private Map<String, Object> nodes;

    public MappingNode(Map<String, Object> nodes) {
        this.nodes = nodes;
    }

    public MappingNode() {
        this.nodes = new HashMap<>();
    }

    public Map<String, Object> getNodes() {
        return nodes;
    }
}
