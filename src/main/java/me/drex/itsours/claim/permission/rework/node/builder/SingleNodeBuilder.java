package me.drex.itsours.claim.permission.rework.node.builder;

import me.drex.itsours.claim.permission.rework.node.AbstractNode;
import me.drex.itsours.claim.permission.rework.node.SingleNode;

public class SingleNodeBuilder extends AbstractNodeBuilder {

    public SingleNodeBuilder(String id) {
        super(id);
    }

    @Override
    public AbstractNode build() {
        return new SingleNode(id, description, childNodes, icon, changePredicate);
    }
}
