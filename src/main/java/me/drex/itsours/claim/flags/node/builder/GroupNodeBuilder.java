package me.drex.itsours.claim.flags.node.builder;

import me.drex.itsours.claim.flags.node.AbstractChildNode;
import me.drex.itsours.claim.flags.node.ChildNode;
import me.drex.itsours.claim.flags.node.GroupNode;

import java.util.LinkedList;
import java.util.List;

public class GroupNodeBuilder extends AbstractNodeBuilder {

    private final List<ChildNode> contained = new LinkedList<>();

    public GroupNodeBuilder(String id) {
        super(id);
    }

    public GroupNodeBuilder contained(List<ChildNode> contained) {
        this.contained.addAll(contained);
        return this;
    }

    @Override
    public AbstractChildNode build() {
        sortChildNodes();
        return new GroupNode(id, description, childNodes, icon, changePredicate, contained);
    }
}
