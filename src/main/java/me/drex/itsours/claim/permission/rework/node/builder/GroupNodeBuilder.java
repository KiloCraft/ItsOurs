package me.drex.itsours.claim.permission.rework.node.builder;

import me.drex.itsours.claim.permission.rework.node.AbstractNode;
import me.drex.itsours.claim.permission.rework.node.GroupNode;
import me.drex.itsours.claim.permission.rework.node.Node;

import java.util.Collection;
import java.util.LinkedList;

public class GroupNodeBuilder extends AbstractNodeBuilder {

    private final Collection<Node> contained = new LinkedList<>();

    public GroupNodeBuilder(String id) {
        super(id);
    }

    public GroupNodeBuilder contained(Collection<Node> contained) {
        this.contained.addAll(contained);
        return this;
    }

    @Override
    public AbstractNode build() {
        return new GroupNode(id, description, childNodes, icon, changePredicate, contained);
    }
}
