package me.drex.itsours.claim.permission.node.builder;

import me.drex.itsours.claim.permission.node.AbstractChildNode;
import me.drex.itsours.claim.permission.node.LiteralNode;

public class LiteralNodeBuilder extends AbstractNodeBuilder {

    public LiteralNodeBuilder(String id) {
        super(id);
    }

    @Override
    public AbstractChildNode build() {
        sortChildNodes();
        return new LiteralNode(id, description, childNodes, icon, changePredicate);
    }
}
