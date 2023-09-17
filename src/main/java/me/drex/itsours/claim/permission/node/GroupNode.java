package me.drex.itsours.claim.permission.node;

import net.minecraft.item.ItemConvertible;
import net.minecraft.text.MutableText;

import java.util.List;
import java.util.function.Predicate;

public class GroupNode extends AbstractChildNode {

    private final List<ChildNode> contained;

    public GroupNode(String id, MutableText description, List<ChildNode> nodes, ItemConvertible icon, Predicate<ChangeContext> changePredicate, List<ChildNode> contained) {
        super(id, description, nodes, icon, changePredicate);
        this.contained = contained;
    }

    @Override
    public String getId() {
        return super.getId().toUpperCase();
    }

    public List<ChildNode> getContained() {
        return contained;
    }

    @Override
    public boolean contains(ChildNode other) {
        for (ChildNode node : contained) {
            if (node.getId().equals(other.getId())) return true;
        }
        return false;
    }

}
