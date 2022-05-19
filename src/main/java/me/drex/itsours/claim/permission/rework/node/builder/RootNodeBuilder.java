package me.drex.itsours.claim.permission.rework.node.builder;

import me.drex.itsours.claim.permission.rework.node.AbstractNode;
import me.drex.itsours.claim.permission.rework.node.RootNode;

public class RootNodeBuilder extends AbstractNodeBuilder {

    private final String name;

    public RootNodeBuilder(String name) {
        super("");
        this.name = name;
    }

    @Override
    public RootNode build() {
        return new RootNode(id, description, childNodes, icon, changePredicate, name);
    }
}
