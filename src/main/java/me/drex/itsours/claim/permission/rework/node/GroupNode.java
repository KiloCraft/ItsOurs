package me.drex.itsours.claim.permission.rework.node;

import net.minecraft.item.ItemConvertible;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

public class GroupNode extends AbstractNode {

    private final Collection<Node> contained;

    public GroupNode(String id, Text description, List<Node> nodes, ItemConvertible icon, Predicate<ChangeContext> changePredicate, Collection<Node> contained) {
        super(id, description, nodes, icon, changePredicate);
        this.contained = contained;
    }

    @Override
    public String getId() {
        return super.getId().toUpperCase();
    }

    @Override
    public boolean contains(Node other) {
        // TODO: Are the nodes actually equal? (or do we need to implement that / use other checks)
        for (Node node : contained) {
            if (node.getId().equals(other.getId())) return true;
        }
        return false;
    }

}
