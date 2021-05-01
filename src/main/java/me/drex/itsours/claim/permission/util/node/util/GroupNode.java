package me.drex.itsours.claim.permission.util.node.util;

import java.util.List;

public class GroupNode extends Node {

    private final List<Node> contains;

    public GroupNode(String id, List<Node> nodes) {
        super(id);
        this.contains = nodes;
    }

    @Override
    public List<Node> getContained() {
        return contains;
    }

    @Override
    public boolean contains(String node) {
        for (Node n : contains) {
            if (n.getId().equals(node)) return true;
        }
        return false;
    }
}
