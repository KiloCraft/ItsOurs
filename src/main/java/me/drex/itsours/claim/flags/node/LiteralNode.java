package me.drex.itsours.claim.flags.node;

import net.minecraft.world.level.ItemLike;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;
import java.util.function.Predicate;

public class LiteralNode extends AbstractChildNode {

    public LiteralNode(String id, MutableComponent description, List<ChildNode> nodes, ItemLike icon, Predicate<ChangeContext> changePredicate) {
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
