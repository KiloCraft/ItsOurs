package me.drex.itsours.claim.permission.util.newNode;

import java.util.Arrays;
import java.util.List;

public class GNode extends Node {

    private List<Node> contains;

    public GNode(String id, Node... nodes) {
        super(id);
        this.contains = Arrays.asList(nodes);
    }

    @Override
    public boolean contains(String node) {
        for (Node n : contains) {
            if (n.getId().equals(node)) return true;
        }
        return false;
    }
}
