package me.drex.itsours.claim.permission.util.node;

import me.drex.itsours.claim.permission.util.node.util.Node;

public class RootNode extends Node {

    public RootNode(String id) {
        super(id);
    }

    @Override
    public String getId() {
        return "";
    }

    @Override
    public String getName() {
        return this.id;
    }
}
