package me.drex.itsours.claim.permission.node.builder;

import me.drex.itsours.claim.permission.node.AbstractNode;
import me.drex.itsours.claim.permission.node.GroupNode;
import me.drex.itsours.claim.permission.node.Node;

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
