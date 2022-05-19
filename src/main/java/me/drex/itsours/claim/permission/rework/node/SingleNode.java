package me.drex.itsours.claim.permission.rework.node;

import net.minecraft.item.ItemConvertible;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Predicate;

public class SingleNode extends AbstractNode {

    public SingleNode(String id, Text description, List<Node> nodes, ItemConvertible icon, Predicate<ChangeContext> changePredicate) {
        super(id, description, nodes, icon, changePredicate);
    }

    @Override
    public String getId() {
        return super.getId().toLowerCase();
    }

    @Override
    public boolean contains(Node other) {
        return this.getId().equals(other.getId());
    }

}
