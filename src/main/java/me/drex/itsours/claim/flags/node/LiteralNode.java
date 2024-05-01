package me.drex.itsours.claim.flags.node;

import net.minecraft.item.ItemConvertible;
import net.minecraft.text.MutableText;

import java.util.List;
import java.util.function.Predicate;

public class LiteralNode extends AbstractChildNode {

    public LiteralNode(String id, MutableText description, List<ChildNode> nodes, ItemConvertible icon, Predicate<ChangeContext> changePredicate) {
        super(id, description, nodes, icon, changePredicate);
    }

    @Override
    public String getId() {
        return super.getId().toLowerCase();
    }

    @Override
    public boolean contains(ChildNode other) {
        return this.getId().equals(other.getId());
    }

}
